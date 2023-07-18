package uk.ac.ebi.eva.evaseqcol.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Repository("NCBISeqColDataSource")
public class NCBISeqColDataSource implements SeqColDataSource{

    private final Logger logger = LoggerFactory.getLogger(NCBISeqColDataSource.class);

    private final NCBIAssemblyDataSource assemblyDataSource;
    private final NCBIAssemblySequenceDataSource assemblySequenceDataSource;
    private DigestCalculator digestCalculator = new DigestCalculator();

    @Autowired
    public NCBISeqColDataSource(NCBIAssemblyDataSource assemblyDataSource,
                                NCBIAssemblySequenceDataSource assemblySequenceDataSource
                                ) {
        this.assemblyDataSource = assemblyDataSource;
        this.assemblySequenceDataSource = assemblySequenceDataSource;
    }

    /**
     * Download both the Assembly Report and the Sequences FASTA file for the given accession
     * and return the seqCol extended data list: names, lengths and sequences */
    public Optional<List<SeqColExtendedDataEntity>> getSeqColExtendedDataListByAccession(
            String accession, SeqColEntity.NamingConvention namingConvention) throws IOException {
        Optional<AssemblyEntity> assemblyEntity = assemblyDataSource.getAssemblyByAccession(accession);
        if (!assemblyEntity.isPresent()) {
            logger.error("Could not fetch Assembly Report from NCBI for accession: " + accession);
            return Optional.empty();
        } else if (!(assemblyEntity.get().getChromosomes() != null && assemblyEntity.get().getChromosomes().size() > 0)) {
            logger.error("No chromosome in assembly " + accession + ". Aborting");
            return Optional.empty();
        }
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession);
        if (!sequenceEntity.isPresent()) {
            logger.error("Could not fetch Sequences FASTA file from NCBI for accession: " + accession);
            return Optional.empty();
        }
        List<SeqColExtendedDataEntity> extendedDataEntities = constructExtendedSeqColDataList(
                assemblyEntity.get(), sequenceEntity.get(), namingConvention, accession);
        return Optional.of(extendedDataEntities);
    }

    @Override
    /**
     * Download both the Assembly Report and the Sequences FASTA file for the given accession
     * and return the seqCol Level one entity for the given naming convention*/
    public Optional<SeqColLevelOneEntity> getSeqColL1ByAssemblyAccession(
            String accession, SeqColEntity.NamingConvention namingConvention) throws IOException {
        Optional<AssemblyEntity> assemblyEntity = assemblyDataSource.getAssemblyByAccession(accession);
        if (!assemblyEntity.isPresent()) {
            logger.error("Could not fetch Assembly Report from NCBI for accession: " + accession);
            return Optional.empty();
        }
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession);
        if (!sequenceEntity.isPresent()) {
            logger.error("Could not fetch Sequences FASTA file from NCBI for accession: " + accession);
            return Optional.empty();
        }
        List<SeqColExtendedDataEntity> extendedDataEntities = constructExtendedSeqColDataList(
                assemblyEntity.get(), sequenceEntity.get(), namingConvention, accession);
        SeqColLevelOneEntity levelOneEntity = constructSeqColLevelOne(extendedDataEntities, namingConvention);
        return Optional.of(levelOneEntity);
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
            }
        }
        levelOneEntity.setObject(jsonLevelOne);
        String digest0 = digestCalculator.generateDigest(levelOneEntity.toString());
        levelOneEntity.setDigest(digest0);
        levelOneEntity.setNamingConvention(convention);
        return levelOneEntity;
    }

    /**
     * Return the 3 extended data objects (names, lengths and sequences) of the given naming convention*/
    public List<SeqColExtendedDataEntity> constructExtendedSeqColDataList(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                                   SeqColEntity.NamingConvention convention, String assemblyAccession) throws IOException {
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        return Arrays.asList(
                constructSeqColSequencesObject(assemblySequenceEntity),
                constructSeqColNamesObject(assemblyEntity, convention),
                constructSeqColLengthsObject(assemblyEntity)
        );
    }

    /**
     * Return the seqCol names array object*/
    public SeqColExtendedDataEntity constructSeqColNamesObject(AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
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
        seqColNamesObject.setObject(seqColNamesArray);
        seqColNamesObject.setDigest(digestCalculator.generateDigest(seqColNamesArray.toString()));
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
        seqColLengthsObject.setObject(seqColLengthsArray);
        seqColLengthsObject.setDigest(digestCalculator.generateDigest(seqColLengthsArray.toString()));
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
        seqColSequencesObject.setObject(seqColSequencesArray);
        seqColSequencesObject.setDigest(digestCalculator.generateDigest(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

}
