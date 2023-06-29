package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelTwoRepository;

import java.util.Optional;

@Service
public class SeqColLevelTwoService {

    @Autowired
    private SeqColLevelTwoRepository repository;

    public Optional<SeqColLevelTwoEntity> addSequenceCollectionL2(SeqColLevelTwoEntity seqColLevelTwo){
        try {
            SeqColLevelTwoEntity seqCol = repository.save(seqColLevelTwo);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            System.out.println("SeqcolL2 with digest " + seqColLevelTwo.getDigest() + " already exists in the db !!");

        }
        return Optional.empty();
    }
}
