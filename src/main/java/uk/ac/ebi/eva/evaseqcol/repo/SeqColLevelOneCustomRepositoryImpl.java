package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SeqColLevelOneCustomRepositoryImpl implements SeqColLevelOneCustomRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<SeqColLevelOneEntity> findByJsonFilters(Map<String, String> filters, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SeqColLevelOneEntity> query = cb.createQuery(SeqColLevelOneEntity.class);
        Root<SeqColLevelOneEntity> root = query.from(SeqColLevelOneEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String jsonKey = filter.getKey();
            String jsonValue = filter.getValue();

            Predicate condition = cb.equal(
                    cb.function("jsonb_extract_path_text", String.class, root.get("seqColLevel1Object"), cb.literal(jsonKey)),
                    jsonValue
            );

            predicates.add(condition);
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        TypedQuery<SeqColLevelOneEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<SeqColLevelOneEntity> results = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SeqColLevelOneEntity> countRoot = countQuery.from(SeqColLevelOneEntity.class);
        countQuery.select(cb.count(countRoot)).where(cb.and(predicates.toArray(new Predicate[0])));
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, count);
    }
}
