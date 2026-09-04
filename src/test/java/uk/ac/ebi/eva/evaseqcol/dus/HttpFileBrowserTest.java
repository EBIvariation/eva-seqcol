package uk.ac.ebi.eva.evaseqcol.dus;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import uk.ac.ebi.eva.evaseqcol.exception.DownloadFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that downloadFile recovers from a connection dropped
 * mid-transfer.
 */
public class HttpFileBrowserTest {

    private static final byte[] PAYLOAD = buildPayload(5000);

    private HttpServer server;

    private static byte[] buildPayload(int size) {
        byte[] payload = new byte[size];
        for (int i = 0; i < size; i++) {
            payload[i] = (byte) ('A' + (i % 26));
        }
        return payload;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void resumesAfterDroppedConnection() throws Exception {
        int breakAt = 2000;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            if (requestCount.incrementAndGet() == 1) {
                // First attempt: write part of the declared body then blow up, simulating a
                // connection dropped mid-transfer (the client sees a truncated fixed-length body).
                exchange.sendResponseHeaders(200, PAYLOAD.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(PAYLOAD, 0, breakAt);
                    os.flush();
                    throw new IOException("simulated broken connection");
                }
            }

            // Second attempt: honour the Range request for the remainder.
            String range = exchange.getRequestHeaders().getFirst("Range");
            assertEquals("bytes=" + breakAt + "-", range);
            byte[] remaining = Arrays.copyOfRange(PAYLOAD, breakAt, PAYLOAD.length);
            exchange.getResponseHeaders().set("ETag", "\"test-etag\"");
            exchange.getResponseHeaders().set("Content-Range",
                    "bytes " + breakAt + "-" + (PAYLOAD.length - 1) + "/" + PAYLOAD.length);
            exchange.sendResponseHeaders(206, remaining.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(remaining);
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            boolean success = new HttpFileBrowser().downloadFile(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt",
                    downloadPath, PAYLOAD.length);

            assertTrue(success);
            assertEquals(2, requestCount.get());
            assertEquals(PAYLOAD.length, Files.size(downloadPath));
            assertArrayEquals(PAYLOAD, Files.readAllBytes(downloadPath));
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

    @Test
    void restartsCleanlyWhenServerIgnoresRange() throws Exception {
        int breakAt = 2000;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            if (requestCount.incrementAndGet() == 1) {
                exchange.sendResponseHeaders(200, PAYLOAD.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(PAYLOAD, 0, breakAt);
                    os.flush();
                    throw new IOException("simulated broken connection");
                }
            }

            // Second attempt: server doesn't support Range - re-sends the full body with 200.
            exchange.sendResponseHeaders(200, PAYLOAD.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(PAYLOAD);
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            boolean success = new HttpFileBrowser().downloadFile(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt",
                    downloadPath, PAYLOAD.length);

            assertTrue(success);
            assertEquals(2, requestCount.get());
            assertEquals(PAYLOAD.length, Files.size(downloadPath));
            assertArrayEquals(PAYLOAD, Files.readAllBytes(downloadPath));
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

    @Test
    void resumesAcrossRepeatedDroppedConnectionsUpToTheAttemptLimit() throws Exception {
        int firstBreakAt = 1500;
        int secondBreakAt = 3200;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            int attempt = requestCount.incrementAndGet();
            String range = exchange.getRequestHeaders().getFirst("Range");

            if (attempt == 1) {
                assertNull(range);
                exchange.sendResponseHeaders(200, PAYLOAD.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(PAYLOAD, 0, firstBreakAt);
                    os.flush();
                    throw new IOException("simulated broken connection (attempt 1)");
                }
            } else if (attempt == 2) {
                assertEquals("bytes=" + firstBreakAt + "-", range);
                byte[] remaining = Arrays.copyOfRange(PAYLOAD, firstBreakAt, PAYLOAD.length);
                exchange.getResponseHeaders().set("ETag", "\"test-etag\"");
                exchange.sendResponseHeaders(206, remaining.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(remaining, 0, secondBreakAt - firstBreakAt);
                    os.flush();
                    throw new IOException("simulated broken connection (attempt 2)");
                }
            } else {
                assertEquals("bytes=" + secondBreakAt + "-", range);
                byte[] remaining = Arrays.copyOfRange(PAYLOAD, secondBreakAt, PAYLOAD.length);
                exchange.getResponseHeaders().set("ETag", "\"test-etag\"");
                exchange.sendResponseHeaders(206, remaining.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(remaining);
                }
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            boolean success = new HttpFileBrowser().downloadFile(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt",
                    downloadPath, PAYLOAD.length);

            assertTrue(success);
            assertEquals(3, requestCount.get());
            assertEquals(PAYLOAD.length, Files.size(downloadPath));
            assertArrayEquals(PAYLOAD, Files.readAllBytes(downloadPath));
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

    @Test
    void givesUpAfterExhaustingAllAttempts() throws Exception {
        int chunkSize = 1000;
        AtomicInteger requestCount = new AtomicInteger();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/file.txt", exchange -> {
            requestCount.incrementAndGet();
            String range = exchange.getRequestHeaders().getFirst("Range");
            int start = range == null ? 0 : Integer.parseInt(range.substring(6, range.length() - 1));
            byte[] remaining = Arrays.copyOfRange(PAYLOAD, start, PAYLOAD.length);
            int writeLen = Math.min(chunkSize, remaining.length);

            // Every attempt drops the connection after a small chunk - none of them ever finish.
            exchange.sendResponseHeaders(start == 0 ? 200 : 206, remaining.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(remaining, 0, writeLen);
                os.flush();
                throw new IOException("simulated broken connection");
            }
        });
        server.start();

        Path downloadPath = Files.createTempFile("http-file-browser-test", ".txt");
        Files.delete(downloadPath);
        try {
            HttpFileBrowser browser = new HttpFileBrowser();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/file.txt";

            assertThrows(DownloadFailedException.class,
                    () -> browser.downloadFile(url, downloadPath, PAYLOAD.length));

            // MAX_DOWNLOAD_ATTEMPTS
            assertEquals(3, requestCount.get());
        } finally {
            Files.deleteIfExists(downloadPath);
        }
    }

}
