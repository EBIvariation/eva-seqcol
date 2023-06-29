package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SeqColLevelOneService {

    @Autowired
    private SeqColLevelOneRepository repository;

    /**
     * Add a new Level 1 sequence collection object and save it to the
     * database*/
    public Optional<SeqColLevelOneEntity> addSequenceCollectionL1(SeqColLevelOneEntity seqColLevelOne){
        SeqColLevelOneEntity seqCol = repository.save(seqColLevelOne);
        return Optional.of(seqCol);
        // TODO: Handle exceptions
    }

    public Optional<SeqColLevelOneEntity> getSeqColL1ByDigest(String digest){
        Optional<SeqColLevelOneEntity> seqColL11 = repository.findById(digest);
        return seqColL11;
    }

    public List<SeqColLevelOneEntity> getAllSeqCollections(){
        return repository.findAll();
    }
}
