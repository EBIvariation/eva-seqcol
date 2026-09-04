package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("h2")
class NCBIBrowserTest {

    @Autowired
    private NCBIBrowserFactory factory;

    private NCBIBrowser ncbiBrowser;

    @BeforeEach
    void setUp() {
        ncbiBrowser = factory.build();
    }

    @Test
    void getGenomeReportDirectoryGCATest() throws IOException, IllegalArgumentException {
        Optional<String> path = ncbiBrowser.getGenomeReportDirectory("GCA_004051055.1");
        assertTrue(path.isPresent());
        assertEquals("/genomes/all/GCA/004/051/055/GCA_004051055.1_ASM405105v1/", path.get());
    }

    @Test
    void getGenomeReportDirectoryGCFTest() throws IOException, IllegalArgumentException {
        Optional<String> path = ncbiBrowser.getGenomeReportDirectory("GCF_007608995.1");
        assertTrue(path.isPresent());
        assertEquals("/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/", path.get());
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        try (InputStream stream = ncbiBrowser.getAssemblyReportInputStream(
                "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/")) {
            assertTrue(stream.read() != -1);
        }
    }

    @Test
    void getNCBIAssemblyReportFile() throws IOException {
        RemoteFile reportFile = ncbiBrowser.getNCBIAssemblyReportFile(
                "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/");
        assertEquals("GCF_007608995.1_ASM760899v1_assembly_report.txt", reportFile.getName());
        assertTrue(reportFile.getSize() > 0);
    }
}
