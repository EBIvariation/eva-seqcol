package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SeqColLevelOneCustomRepository {
    Page<String> findAllDigestsByJsonFilters(Map<String, String> filters, Pageable pageable);
}
