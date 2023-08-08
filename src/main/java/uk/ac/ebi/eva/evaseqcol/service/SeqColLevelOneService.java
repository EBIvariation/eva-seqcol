package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.refget.SHA512ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelOneRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;
import java.util.Arrays;
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
    }

    public Optional<SeqColLevelOneEntity> getSeqColLevelOneByDigest(String digest){
        SeqColLevelOneEntity seqColL11 = repository.findSeqColLevelOneEntityByDigest(digest);
        if (seqColL11 != null) {
            return Optional.of(seqColL11);
        } else {
            return Optional.empty();
        }
    }

    public void removeSeqColLevelOneByDigest(String digest) {
        repository.removeSeqColLevelOneEntityByDigest(digest);
    }

    public void removeAllSeqCols() {
        repository.deleteAll();
    }

    public long countSeqColLevelOneEntitiesByDigest(String digest) {
        return repository.countSeqColLevelOneEntitiesByDigest(digest);
    }
    public List<SeqColLevelOneEntity> getAllSeqColLevelOneObjects(){
        return repository.findAll();
    }

    /**
     * Construct a seqCol level 1 entity out of three seqCol level 2 entities that
     * hold names, lengths and sequences objects*/
    public SeqColLevelOneEntity constructSeqColLevelOne(List<SeqColExtendedDataEntity> extendedDataEntities,
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
                case md5DigestsOfSequences:
                    jsonLevelOne.setMd5DigestsOfSequences(dataEntity.getDigest());
                    break;
            }
        }
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        String digest0 = digestCalculator.getSha512Digest(levelOneEntity.toString());
        levelOneEntity.setDigest(digest0);
        levelOneEntity.setNamingConvention(convention);
        return levelOneEntity;
    }

    /**
     * Construct a Level 1 seqCol out of a Level 2 seqCol*/
    public SeqColLevelOneEntity constructSeqColLevelOne(
            SeqColLevelTwoEntity levelTwoEntity, SeqColEntity.NamingConvention convention) throws IOException {
        SHA512ChecksumCalculator sha512Calculator = new SHA512ChecksumCalculator();
        JSONExtData sequencesExtData = new JSONExtData(levelTwoEntity.getSequences());
        JSONExtData lengthsExtData = new JSONExtData(levelTwoEntity.getLengths());
        JSONExtData namesExtData = new JSONExtData(levelTwoEntity.getNames());
        JSONExtData md5SequencesExtData = new JSONExtData(levelTwoEntity.getMd5Sequences());

        // Sequences
        SeqColExtendedDataEntity sequencesExtEntity = new SeqColExtendedDataEntity();
        sequencesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.sequences);
        sequencesExtEntity.setExtendedSeqColData(sequencesExtData);
        sequencesExtEntity.setDigest(sha512Calculator.calculateChecksum(sequencesExtData.toString()));
        // Md5Sequences
        SeqColExtendedDataEntity md5SequencesExtEntity = new SeqColExtendedDataEntity();
        md5SequencesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);
        md5SequencesExtEntity.setExtendedSeqColData(md5SequencesExtData);
        md5SequencesExtEntity.setDigest(sha512Calculator.calculateChecksum(md5SequencesExtData.toString()));
        // Lengths
        SeqColExtendedDataEntity lengthsExtEntity = new SeqColExtendedDataEntity();
        lengthsExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.lengths);
        lengthsExtEntity.setExtendedSeqColData(lengthsExtData);
        lengthsExtEntity.setDigest(sha512Calculator.calculateChecksum(lengthsExtData.toString()));
        // Names
        SeqColExtendedDataEntity namesExtEntity = new SeqColExtendedDataEntity();
        namesExtEntity.setAttributeType(SeqColExtendedDataEntity.AttributeType.names);
        namesExtEntity.setExtendedSeqColData(namesExtData);
        namesExtEntity.setDigest(sha512Calculator.calculateChecksum(namesExtData.toString()));

        List<SeqColExtendedDataEntity> extendedDataEntities = Arrays.asList(
                sequencesExtEntity,
                md5SequencesExtEntity,
                lengthsExtEntity,
                namesExtEntity
        );
        return constructSeqColLevelOne(extendedDataEntities, convention);
    }
}
