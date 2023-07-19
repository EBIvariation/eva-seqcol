package uk.ac.ebi.eva.evaseqcol.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class NCBISeqColDataSourceTest {

    private final String GCA_ACCESSION = "GCA_000146045.2";
    private final SeqColEntity.NamingConvention NAMING_CONVENTION = SeqColEntity.NamingConvention.GENBANK;

    @Autowired
    private NCBISeqColDataSource ncbiSeqColDataSource;

    @Test
    void getSeqColL1ByAssemblyAccession() throws IOException {
        Optional<SeqColLevelOneEntity> levelOneEntity = ncbiSeqColDataSource.getSeqColL1ByAssemblyAccession(
                GCA_ACCESSION, NAMING_CONVENTION);
        assertTrue(levelOneEntity.isPresent());
        assertFalse(levelOneEntity.get().getSeqColLevel1Object().getSequences().isEmpty());
    }
}