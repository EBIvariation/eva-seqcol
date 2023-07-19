package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.exception.ExtendedDataNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.SHA512Calculator;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColExtendedDataRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColExtendedDataService {

    @Autowired
    private SeqColExtendedDataRepository repository;

    private ChecksumCalculator sha512Calculator = new SHA512Calculator();

    /**
     * Add a seqCol's attribute; names, lengths or sequences, to the database*/
    public Optional<SeqColExtendedDataEntity> addSeqColExtendedData(SeqColExtendedDataEntity seqColExtendedData){
        try {
            SeqColExtendedDataEntity seqCol = repository.save(seqColExtendedData);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Return the list of all extended data objects (level 2) given the seqcol's level 0 digest*/
    public Optional<List<String>> getSeqColExtendedDataByLevelZeroDigest(String digestL0) {
        Optional<List<String>> seqColExtendedDataList =  repository.getSeqColExtendedDataByLevel0Digest(digestL0);
        if (seqColExtendedDataList.isPresent()) {
            return seqColExtendedDataList;
        }else {
            throw new SeqColNotFoundException(digestL0);
        }
    }

    /**
     * Return the extended data object (level 2) that corresponds to the given digest*/
    public Optional<SeqColExtendedDataEntity> getSeqColExtendedDataEntityByDigest(String digest) {
        Optional<SeqColExtendedDataEntity> extendedDataEntity = repository.getSeqColExtendedDataEntityByDigest(digest);
        if (extendedDataEntity.isPresent()) {
            return extendedDataEntity;
        } else {
            throw new ExtendedDataNotFoundException(digest);
        }
    }

    @Transactional
    public List<SeqColExtendedDataEntity> addAll(List<SeqColExtendedDataEntity> seqColExtendedDataList) {
        return repository.saveAll(seqColExtendedDataList);
    }

    /**
     * Return the extendedData object for the given digest*/
    Optional<SeqColExtendedDataEntity> getExtendedAttributeByDigest(String digest) {
        SeqColExtendedDataEntity dataEntity = repository.findSeqColExtendedDataEntityByDigest(digest);
        return Optional.of(dataEntity);
    }


    /**
     * Return the seqCol names array object*/
    SeqColExtendedDataEntity constructSeqColNamesObject(AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
        SeqColExtendedDataEntity seqColNamesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
        JSONExtData seqColNamesArray = new JSONExtData();
        List<String> namesList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            switch (convention) {
                case ENA:
                    namesList.add(chromosome.getEnaSequenceName());
                    break;
                case GENBANK:
                    namesList.add(chromosome.getGenbankSequenceName());
                    break;
                case UCSC:
                    namesList.add(chromosome.getUcscName());
                    break;
            }
        }

        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setExtendedSeqColData(seqColNamesArray);
        seqColNamesObject.setDigest(sha512Calculator.calculateChecksum(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    /**
     * Return the seqCol lengths array object*/
    public SeqColExtendedDataEntity constructSeqColLengthsObject(AssemblyEntity assemblyEntity) throws IOException {
        SeqColExtendedDataEntity seqColLengthsObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData seqColLengthsArray = new JSONExtData();
        List<String> lengthsList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            lengthsList.add(chromosome.getSeqLength().toString());
        }
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setExtendedSeqColData(seqColLengthsArray);
        seqColLengthsObject.setDigest(sha512Calculator.calculateChecksum(seqColLengthsArray.toString()));
        return seqColLengthsObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public SeqColExtendedDataEntity constructSeqColSequencesObject(AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity seqColSequencesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData seqColSequencesArray = new JSONExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequenceMD5());
        }
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(sha512Calculator.calculateChecksum(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the 3 extended data objects (names, lengths and sequences) of the given naming convention*/
    public List<SeqColExtendedDataEntity> constructExtendedSeqColDataList(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                            SeqColEntity.NamingConvention convention, String assemblyAccession) throws IOException {
        return Arrays.asList(
                constructSeqColSequencesObject(assemblySequenceEntity),
                constructSeqColNamesObject(assemblyEntity, convention),
                constructSeqColLengthsObject(assemblyEntity)
        );
    }

    /**
     * Construct and return a Level Two (with exploded data) SeqCol entity out of the given assemblyEntity and the
     * assemblySequencesEntity*/
    public SeqColLevelTwoEntity constructSeqColLevelTwo(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                 SeqColEntity.NamingConvention convention, String accession) throws IOException {
        SeqColLevelTwoEntity seqColLevelTwo = new SeqColLevelTwoEntity();
        SeqColExtendedDataEntity extendedNamesData = constructSeqColNamesObject(assemblyEntity, convention);
        SeqColExtendedDataEntity extendedLengthsData = constructSeqColLengthsObject(assemblyEntity);
        SeqColExtendedDataEntity extendedSequencesData = constructSeqColSequencesObject(assemblySequenceEntity);
        seqColLevelTwo.setNames(extendedNamesData.getExtendedSeqColData().getObject());
        seqColLevelTwo.setLengths(extendedLengthsData.getExtendedSeqColData().getObject());
        seqColLevelTwo.setSequences(extendedSequencesData.getExtendedSeqColData().getObject());
        return seqColLevelTwo;
    }
}
