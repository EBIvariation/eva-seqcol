package uk.ac.ebi.eva.evaseqcol.datasource;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.io.IOException;
import java.util.Optional;


public interface SeqColDataSource {
    Optional<SeqColLevelOneEntity> getSeqColL1ByAssemblyAccession(
            String accesison, SeqColEntity.NamingConvention namingConvention) throws IOException;
}
