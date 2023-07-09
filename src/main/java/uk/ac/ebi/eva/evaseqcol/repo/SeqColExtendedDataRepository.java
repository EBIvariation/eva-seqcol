package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;

@Repository
public interface SeqColExtendedDataRepository extends JpaRepository<SeqColExtendedDataEntity, String> {

}
