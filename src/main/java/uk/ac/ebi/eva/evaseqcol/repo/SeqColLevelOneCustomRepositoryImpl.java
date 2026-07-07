package uk.ac.ebi.eva.evaseqcol.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SeqColLevelOneCustomRepositoryImpl implements SeqColLevelOneCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<String> findAllDigestsByJsonFilters(Map<String, String> filters, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<SeqColLevelOneEntity> root = query.from(SeqColLevelOneEntity.class);
        query.select(root.get("digest")).where(cb.and(buildPredicates(cb, root, filters)));

        List<String> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SeqColLevelOneEntity> countRoot = countQuery.from(SeqColLevelOneEntity.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(buildPredicates(cb, countRoot, filters)));
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, count);
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<SeqColLevelOneEntity> root,
                                        Map<String, String> filters) {
        List<Predicate> predicates = new ArrayList<>();
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            predicates.add(cb.equal(
                    cb.function("jsonb_extract_path_text", String.class,
                            root.get("seqColLevel1Object"), cb.literal(filter.getKey())),
                    filter.getValue()
            ));
        }
        return predicates.toArray(new Predicate[0]);
    }
}