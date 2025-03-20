package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.util.Map;

public interface SeqColLevelOneCustomRepository {
    Page<SeqColLevelOneEntity> findByJsonFilters(Map<String, String> filters, Pageable pageable);
}
