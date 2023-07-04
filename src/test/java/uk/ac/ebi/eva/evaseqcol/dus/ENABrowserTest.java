package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class ENABrowserTest {

    @Autowired
    private ENABrowserFactory factory;

    private ENABrowser enaBrowser;

    @BeforeEach
    void setUp() throws IOException {
        enaBrowser = factory.build();
        enaBrowser.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        enaBrowser.disconnect();
    }

    @Test
    void connect() throws IOException {
        enaBrowser.connect();
    }

    @Test
    void navigateToENAAssemblyDirectory() throws IOException {
        assertTrue(enaBrowser.changeWorkingDirectory(ENABrowser.PATH_ENA_ASSEMBLY));
        assertTrue(enaBrowser.listFiles().length > 0);
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        try (InputStream stream = enaBrowser.getAssemblyReportInputStream("GCA_003005035.1")) {
            assertTrue(stream.read() != -1);
        }
    }
}