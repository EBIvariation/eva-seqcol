package uk.ac.ebi.eva.evaseqcol.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;

import java.util.Optional;

public interface SeqColExtendedDataRepository extends JpaRepository<SeqColExtendedDataEntity<?>, String> {

    <T> Optional<SeqColExtendedDataEntity<T>> findByDigest(String digest);
}
