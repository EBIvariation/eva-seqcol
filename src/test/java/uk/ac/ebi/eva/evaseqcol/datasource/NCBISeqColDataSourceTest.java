package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("h2")
class NCBISeqColDataSourceTest {

    private final String GCA_ACCESSION = "GCA_000146045.2";

    @Autowired
    private NCBISeqColDataSource ncbiSeqColDataSource;

    @Test
    void getSeqColL1ByAssemblyAccession() throws IOException {
        Optional<SeqColSourceData> fetchSeqColData = ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(GCA_ACCESSION);
        assertTrue(fetchSeqColData.isPresent());
        List<SeqColExtendedDataEntity<List<String>>> namesAttributesList = fetchSeqColData.get().namesPerNamingConvention();
        SeqColExtendedDataEntity<List<Integer>> lengthsAttribute = fetchSeqColData.get().lengths();
        SeqColExtendedDataEntity<List<String>> sequenceAttribute = fetchSeqColData.get().sequences();
        assertNotNull(lengthsAttribute);
        assertNotNull(sequenceAttribute);
        assertEquals(lengthsAttribute.getExtendedSeqColData().getObject().size(), sequenceAttribute.getExtendedSeqColData().getObject().size());
        // We can retrieve sequences with two naming convention from this assembly report ('GCA_ACCESSION')
        // Which are GENBANK and UCSC
        assertEquals(2, namesAttributesList.size());
    }
}