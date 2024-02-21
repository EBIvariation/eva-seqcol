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
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.SHA512ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("NCBISeqColDataSource")
public class NCBISeqColDataSource implements SeqColDataSource{

    private final Logger logger = LoggerFactory.getLogger(NCBISeqColDataSource.class);
    private final NCBIAssemblyDataSource assemblyDataSource;
    private final NCBIAssemblySequenceDataSource assemblySequenceDataSource;
    private DigestCalculator digestCalculator = new DigestCalculator();
    private SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
    private MD5ChecksumCalculator md5Caclculator = new MD5ChecksumCalculator();

    @Autowired
    public NCBISeqColDataSource(NCBIAssemblyDataSource assemblyDataSource,
                                NCBIAssemblySequenceDataSource assemblySequenceDataSource
                                ) {
        this.assemblyDataSource = assemblyDataSource;
        this.assemblySequenceDataSource = assemblySequenceDataSource;}

    @Override
    /**
     * Download both the Assembly Report and the Sequences FASTA file for the given accession
     * and return a Map with the following content:
     *          {
     *              "sameValueAttributes" : { // Another Map
     *                      "extendedLengths" : [...],
     *                      "extendedSequences": [...],
     *                      "extendedMd5Sequences": [...]
     *                      }
     *              "namesAttributes" : [extendedNames1, extendedNames2, ...]
     *          }
     * The "sameValueAttributes" are the attributes that have the same value across multiple seqCol for the same assembly
     * accession.
     * The "namesAttributes" has the list of the list of sequences' names with all possible naming conventions.*/
    public Optional<Map<String, Object>> getAllPossibleSeqColExtendedData(String accession) throws IOException {
        Map<String, Object> seqColResultData = new HashMap<>();

        // Fetching Assembly Entity (Assembly Report)
        Optional<AssemblyEntity> assemblyEntity = assemblyDataSource.getAssemblyByAccession(accession);
        if (!assemblyEntity.isPresent()) {
            logger.error("Could not fetch Assembly Report from NCBI for assembly accession: " + accession);
            return Optional.empty();
        } else if (!(assemblyEntity.get().getChromosomes() != null && !assemblyEntity.get().getChromosomes().isEmpty())) {
            logger.error("No chromosome in assembly " + accession + ". Aborting");
            return Optional.empty();
        }

        // Fetching Sequence Entity (FASTA File)
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession);
        if (!sequenceEntity.isPresent()) {
            logger.error("Could not fetch Sequences FASTA file from NCBI for assembly accession: " + accession);
            return Optional.empty();
        }
        logger.info("Assembly report and FASTA file have been fetched and parsed successfully");

        // Same Value Attribute Map
        Map<String, Object> sameValueAttributesMap = new HashMap<>(); // Content Example: {"extendedLengths": SeqColExtendedDataEntity<List<Integer>>, ...}
        sameValueAttributesMap.put("extendedLengths", SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity.get()));
        sameValueAttributesMap.put("extendedSequences", SeqColExtendedDataEntity.constructSeqColSequencesObject(sequenceEntity.get()));
        sameValueAttributesMap.put("extendedMd5Sequences", SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(sequenceEntity.get()));

        // Seqcol Result Data Map
        seqColResultData.put("sameValueAttributes", sameValueAttributesMap);
        seqColResultData.put("namesAttributes", SeqColExtendedDataEntity.constructAllPossibleExtendedNamesSeqColData(assemblyEntity.get()));

        return Optional.of(seqColResultData);
    }

    public Optional<Map<String, Object>> getAllPossibleSeqColExtendedData(String accession, String fastaFileContent) throws IOException {
        Map<String, Object> seqColResultData = new HashMap<>();

        // Fetching Sequence Entity (FASTA File)
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession, fastaFileContent);
        if (!sequenceEntity.isPresent()) {
            logger.error("Could not parse FASTA file content: ");
            return Optional.empty();
        }
        logger.info("FASTA file have been parsed successfully");

        Map<String, Object> sameValueAttributesMap = new HashMap<>();
        sameValueAttributesMap.put("extendedLengths", SeqColExtendedDataEntity.constructSeqColLengthsObject(sequenceEntity.get()));
        sameValueAttributesMap.put("extendedSequences", SeqColExtendedDataEntity.constructSeqColSequencesObject(sequenceEntity.get()));
        sameValueAttributesMap.put("extendedMd5Sequences", SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(sequenceEntity.get()));

        // Seqcol Result Data Map
        seqColResultData.put("sameValueAttributes", sameValueAttributesMap);
        seqColResultData.put("namesAttributes", Collections.singletonList(SeqColExtendedDataEntity
                .constructSeqColNamesObjectByNamingConvention(sequenceEntity.get(), SeqColEntity.NamingConvention.TEST)));
        return Optional.of(seqColResultData);
    }
}
