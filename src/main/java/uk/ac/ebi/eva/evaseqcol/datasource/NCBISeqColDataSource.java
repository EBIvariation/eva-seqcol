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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("NCBISeqColDataSource")
public class NCBISeqColDataSource{

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

    //@Override
    /**
     * Download both the Assembly Report and the Sequences FASTA file for the given accession
     * and return a Map with the following content:
     *          {
     *              "sameValueAttributes" : [extendedLengths, extendedSequences, extendedMd5Sequences],
     *              "namesAttributes" : [extendedNames1, extendedNames2, ...]
     *          }
     * The "sameValueAttributes" are the attributes that have the same value across multiple seqCol for the same assembly
     * accession.
     * The "namesAttributes" has the list of the list of sequences' names with all possible naming conventions.*/
    // TODO: REFACTOR
    /*public Optional<Map<String, List<SeqColExtendedDataEntity>>> getAllPossibleSeqColExtendedData(String accession) throws IOException {
        Map<String, List<SeqColExtendedDataEntity>> seqColResultData = new HashMap<>();
        Optional<AssemblyEntity> assemblyEntity = assemblyDataSource.getAssemblyByAccession(accession);
        if (!assemblyEntity.isPresent()) {
            logger.error("Could not fetch Assembly Report from NCBI for assembly accession: " + accession);
            return Optional.empty();
        } else if (!(assemblyEntity.get().getChromosomes() != null && !assemblyEntity.get().getChromosomes().isEmpty())) {
            logger.error("No chromosome in assembly " + accession + ". Aborting");
            return Optional.empty();
        }
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession);
        if (!sequenceEntity.isPresent()) {
            logger.error("Could not fetch Sequences FASTA file from NCBI for assembly accession: " + accession);
            return Optional.empty();
        }
        logger.info("Assembly report and FASTA file have been fetched and parsed successfully");
        seqColResultData.put(
                "sameValueAttributes",
                SeqColExtendedDataEntity.constructSameValueExtendedSeqColData(assemblyEntity.get(), sequenceEntity.get()));
        seqColResultData.put(
                "namesAttributes",
                SeqColExtendedDataEntity.constructAllPossibleExtendedNamesSeqColData(assemblyEntity.get()));
        return Optional.of(seqColResultData);
    }*/
}
