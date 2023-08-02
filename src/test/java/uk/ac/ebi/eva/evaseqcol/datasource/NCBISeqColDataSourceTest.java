package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class NCBISeqColDataSourceTest {

    private final String GCA_ACCESSION = "GCA_000146045.2";

    @Autowired
    private NCBISeqColDataSource ncbiSeqColDataSource;

    @Test
    void getSeqColL1ByAssemblyAccession() throws IOException {
        Optional<Map<String, List<SeqColExtendedDataEntity>>> fetchSeqColData = ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(GCA_ACCESSION);
        assertTrue(fetchSeqColData.isPresent());
        assertFalse(fetchSeqColData.get().get("namesAttributes").isEmpty());
        // We can retrieve sequences with two naming convention from this assembly report ('GCA_ACCESSION')
        // Which are GENBANK and UCSC
        assertTrue(fetchSeqColData.get().get("namesAttributes").size() == 2);
    }
}