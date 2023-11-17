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
        Optional<Map<String, Object>> fetchSeqColData = ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(GCA_ACCESSION);
        assertTrue(fetchSeqColData.isPresent());
        Map<String, Object> sameValueAttributesMap = (Map<String, Object>) fetchSeqColData.get().get("sameValueAttributes");
        List<SeqColExtendedDataEntity<List<String>>> namesAttributesList = (List<SeqColExtendedDataEntity<List<String>>>) fetchSeqColData.get().get("namesAttributes");
        SeqColExtendedDataEntity<List<Integer>> lengthsAttribute = (SeqColExtendedDataEntity<List<Integer>>) sameValueAttributesMap.get("extendedLengths");
        SeqColExtendedDataEntity<List<String>> sequenceAttribute = (SeqColExtendedDataEntity<List<String>>) sameValueAttributesMap.get("extendedSequences");
        assertNotNull(lengthsAttribute);
        assertNotNull(sequenceAttribute);
        assertEquals(lengthsAttribute.getExtendedSeqColData().getObject().size(), sequenceAttribute.getExtendedSeqColData().getObject().size());
        // We can retrieve sequences with two naming convention from this assembly report ('GCA_ACCESSION')
        // Which are GENBANK and UCSC
        assertEquals(2, namesAttributesList.size());
    }
}