package uk.ac.ebi.eva.evaseqcol.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class NCBIBrowserTest {

    @Autowired
    private NCBIBrowserFactory factory;

    private NCBIBrowser ncbiBrowser;

    @BeforeEach
    void setUp() throws IOException {
        ncbiBrowser = factory.build();
        ncbiBrowser.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        ncbiBrowser.disconnect();
    }

    @Test
    void connect() throws IOException {
        ncbiBrowser.connect();
    }

    @Test
    void navigateToAllGenomesDirectory() throws IOException {
        assertTrue(ncbiBrowser.changeWorkingDirectory(NCBIBrowser.PATH_GENOMES_ALL));
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

    @Test
    void navigateToSubDirectoryPath() throws IOException {
        ncbiBrowser.changeWorkingDirectory("/genomes/INFLUENZA/");
        assertTrue(ncbiBrowser.listFiles().length > 0);
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
}