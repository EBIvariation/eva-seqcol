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
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;
import uk.ac.ebi.eva.evaseqcol.refget.SHA512Calculator;
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
    private ChecksumCalculator sha512Calculator = new SHA512Calculator();
    private ChecksumCalculator md5Caclculator = new MD5Calculator();

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
                case sequencesMD5:
                    jsonLevelOne.setMd5Sequences(dataEntity.getDigest());
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
     * Return the 3 extended data objects (names, lengths and sequences) of the given naming convention*/
    public List<SeqColExtendedDataEntity> constructExtendedSeqColDataList(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                                   SeqColEntity.NamingConvention convention, String assemblyAccession) throws IOException {
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        return Arrays.asList(
                SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColNamesObject(assemblyEntity, convention),
                SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity)
        );
    }

}
