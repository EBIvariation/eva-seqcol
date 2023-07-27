package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColLevelTwoService {

    @Autowired
    private SeqColExtendedDataService extendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    /**
     * Make 2 recursive lookups to retrieve and construct the seqCol level 2 object
     * @param digest: level 0 seqCol digest*/
    public Optional<SeqColLevelTwoEntity> getSeqColLevelTwoByDigest(String digest) {
        // 1 DATABASE LOOKUP
        Optional<SeqColLevelOneEntity> levelOneEntity = levelOneService.getSeqColLevelOneByDigest(digest);
        if (!levelOneEntity.isPresent()) {
            //TODO THROW EXCEPTION
            System.out.println("EXCPETION: seqCol with digest: " + digest + "doesn't exists !");
            return Optional.empty();
        }
        // 2 DATABASE LOOKUPS
        List<SeqColExtendedDataEntity> extendedAttributes = getExtendedAttributes(levelOneEntity.get());
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        for (SeqColExtendedDataEntity extendedData: extendedAttributes) {
            switch (extendedData.getAttributeType()) {
                case lengths:
                    levelTwoEntity.setLengths(extendedData.getExtendedSeqColData().getObject());
                    break;
                case names:
                    levelTwoEntity.setNames(extendedData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences(extendedData.getExtendedSeqColData().getObject());
                    break;
                case md5DigestsOfSequences:
                    levelTwoEntity.setMd5Sequences(extendedData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        return Optional.of(levelTwoEntity);
    }

    /**
     * Return the list of the extended (exploded) seqCol attributes; names, lengths and sequences
     * Given the corresponding seqCol level 1 object*/
    private List<SeqColExtendedDataEntity> getExtendedAttributes(SeqColLevelOneEntity levelOneEntity) {
        Optional<SeqColExtendedDataEntity> extendedSequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getSequences());
        if (!extendedSequences.isPresent()) {
            throw new RuntimeException("Extended sequences data with digest: " + levelOneEntity.getSeqColLevel1Object().getSequences() + " not found");
        }
        extendedSequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.sequences);

        Optional<SeqColExtendedDataEntity> extendedMD5Sequences = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences());
        if (!extendedMD5Sequences.isPresent()) {
            throw new RuntimeException("Extended md5 sequences data with digest:" + levelOneEntity.getSeqColLevel1Object().getMd5DigestsOfSequences() + " not found");
        }
        extendedMD5Sequences.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.md5DigestsOfSequences);

        Optional<SeqColExtendedDataEntity> extendedLengths = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getLengths());
        if (!extendedLengths.isPresent()) {
            throw new RuntimeException("Extended lengths data with digest: " + levelOneEntity.getSeqColLevel1Object().getLengths() + " not found");
        }
        extendedLengths.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.lengths);

        Optional<SeqColExtendedDataEntity> extendedNames = extendedDataService.getExtendedAttributeByDigest(levelOneEntity.getSeqColLevel1Object().getNames());
        if (!extendedNames.isPresent()) {
            throw new RuntimeException("Extended names data with digest: " + levelOneEntity.getSeqColLevel1Object().getNames() + " not found");
        }
        extendedNames.get().setAttributeType(SeqColExtendedDataEntity.AttributeType.names);

        return Arrays.asList(
                extendedSequences.get(),
                extendedMD5Sequences.get(),
                extendedLengths.get(),
                extendedNames.get()
        );
    }

    public SeqColLevelTwoEntity constructSeqColL2(String level0Digest, List<SeqColExtendedDataEntity> extendedDataEntities) {
        SeqColLevelTwoEntity levelTwoEntity = new SeqColLevelTwoEntity();
        levelTwoEntity.setDigest(level0Digest);
        for (SeqColExtendedDataEntity extendedData: extendedDataEntities) {
            switch (extendedData.getAttributeType()) {
                case lengths:
                    levelTwoEntity.setLengths(extendedData.getExtendedSeqColData().getObject());
                    break;
                case names:
                    levelTwoEntity.setNames(extendedData.getExtendedSeqColData().getObject());
                    break;
                case sequences:
                    levelTwoEntity.setSequences(extendedData.getExtendedSeqColData().getObject());
                    break;
                case sequencesMD5:
                    levelTwoEntity.setMd5Sequences(extendedData.getExtendedSeqColData().getObject());
                    break;
            }
        }
        return levelTwoEntity;
    }
}
