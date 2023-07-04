package uk.ac.ebi.eva.evaseqcol.dus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PassiveAnonymousFTPClientTest {

    private static final String SERVER_NCBI = "ftp.ncbi.nlm.nih.gov";

    @Nested
    class WithSetupAndTeardown {

        private PassiveAnonymousFTPClient ftpClient;

        @BeforeEach
        void setUp() throws IOException {
            ftpClient = new PassiveAnonymousFTPClient();
            ftpClient.connect(SERVER_NCBI);
        }

        @AfterEach
        void tearDown() throws IOException {
            ftpClient.disconnect();
        }

        @Test
        void changeDirectory() throws IOException {
            ftpClient.changeWorkingDirectory("genomes");
        }

        @Test
        void changeDirectoryAndList() throws IOException {
            ftpClient.changeWorkingDirectory("genomes");
            FTPFile[] ftpFiles = ftpClient.listFiles();
            assertTrue(ftpFiles.length > 0);
        }

        @Test
        void changeToNestedDirectoryAndFindAssemblyReport() throws IOException {
            ftpClient.changeWorkingDirectory("genomes/all/GCA/000/002/305/GCA_000002305.1_EquCab2.0/");
            FTPFile[] ftpFiles = ftpClient.listFiles();
            assertTrue(ftpFiles.length > 0);
            String assemblyReport = "GCA_000002305.1_EquCab2.0_assembly_report.txt";
            boolean found = Stream.of(ftpFiles)
                                  .anyMatch(f -> f.getName().contains(assemblyReport));
            assertTrue(found, "didn't find the assembly report '" + assemblyReport + "' in the folder. Contents are:\n"
                    + Stream.of(ftpFiles).map(FTPFile::toString).collect(Collectors.joining("\n")));
        }

        @Test
        void listDirectories() throws IOException {
            FTPFile[] ftpFiles = ftpClient.listDirectories();
            assertTrue(ftpFiles.length > 0);
        }

    }

    @Nested
    class WithoutSetupAndTeardown {

        private final int PORT_NCBI_FTP = 21;

        @Test
        void connectToServerWithExplicitPort() throws IOException {
            PassiveAnonymousFTPClient passiveAnonymousFtpClient = new PassiveAnonymousFTPClient();
            try {
                passiveAnonymousFtpClient.connect(SERVER_NCBI, PORT_NCBI_FTP);
                FTPFile[] ftpFiles = passiveAnonymousFtpClient.listFiles();
                assertTrue(ftpFiles.length > 0);
            } finally {
                passiveAnonymousFtpClient.disconnect();
            }
        }

        @Test
        void FTPClientTest() throws IOException {
            FTPClient ftp = new FTPClient();
            try {
                ftp.connect(SERVER_NCBI, PORT_NCBI_FTP);
                ftp.enterLocalPassiveMode();
                boolean login = ftp.login("anonymous", "anonymous");
                assertTrue(login);
                FTPFile[] ftpFiles = ftp.listDirectories();
                assertTrue(ftpFiles.length > 0);
            } finally {
                ftp.disconnect();
            }
        }

    }
}