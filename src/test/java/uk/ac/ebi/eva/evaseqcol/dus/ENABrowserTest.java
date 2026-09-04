package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("h2")
@SpringBootTest
class ENABrowserTest {

    @Autowired
    private ENABrowserFactory factory;

    private ENABrowser enaBrowser;

    @BeforeEach
    void setUp() {
        enaBrowser = factory.build();
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        try (InputStream stream = enaBrowser.getAssemblyReportInputStream("GCA_003005035.1")) {
            assertTrue(stream.read() != -1);
        }
    }

    @Test
    void getAssemblyReportFile() throws IOException {
        String accession = "GCA_003005035.1";
        String dirPath = enaBrowser.getAssemblyDirPath(accession);
        RemoteFile reportFile = enaBrowser.getAssemblyReportFile(dirPath, accession);
        assertEquals(accession + "_sequence_report.txt", reportFile.getName());
        assertTrue(reportFile.getSize() > 0);
    }
}
