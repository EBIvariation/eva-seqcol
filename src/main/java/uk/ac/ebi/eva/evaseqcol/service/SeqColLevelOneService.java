package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.refget.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColLevelOneService {

    @Autowired
    private SeqColLevelOneRepository repository;

    private DigestCalculator digestCalculator = new DigestCalculator();

    /**
     * Add a new Level 1 sequence collection object and save it to the
     * database*/
    public Optional<SeqColLevelOneEntity> addSequenceCollectionL1(SeqColLevelOneEntity seqColLevelOne){
        SeqColLevelOneEntity seqCol = repository.save(seqColLevelOne);
        return Optional.of(seqCol);
        // TODO: Handle exceptions
    }


    public Optional<SeqColLevelOneEntity> getSeqColLevelOneByDigest(String digest){
        SeqColLevelOneEntity seqColL11 = repository.findSeqColLevelOneEntityByDigest(digest);
        if (seqColL11 != null) {
            return Optional.of(seqColL11);
        }
        return Optional.empty();
    }

    public List<SeqColLevelOneEntity> getAllSeqColLevelOneObjects(){
        return repository.findAll();
    }

    /**
     * Construct a seqCol level 1 entity out of three seqCol level 2 entities that
     * hold names, lengths and sequences objects*/
    SeqColLevelOneEntity constructSeqColLevelOne(List<SeqColExtendedDataEntity> extendedDataEntities,
                                                 SeqColEntity.NamingConvention convention) throws IOException {
        SeqColLevelOneEntity levelOneEntity = new SeqColLevelOneEntity();
        JSONLevelOne jsonLevelOne = new JSONLevelOne();
        for (SeqColExtendedDataEntity dataEntity: extendedDataEntities) {
            switch (dataEntity.getAttributeType()) {
                case lengths:
                    jsonLevelOne.setLengths(dataEntity.getDigest());
                    break;
                case names:
                    jsonLevelOne.setNames(dataEntity.getDigest());
                    break;
                case sequences:
                    jsonLevelOne.setSequences(dataEntity.getDigest());
                    break;
            }
        }
        levelOneEntity.setObject(jsonLevelOne);
        String digest0 = digestCalculator.generateDigest(levelOneEntity.toString());
        levelOneEntity.setDigest(digest0);
        levelOneEntity.setNamingConvention(convention);
        return levelOneEntity;
    }


}
