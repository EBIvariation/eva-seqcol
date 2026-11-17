package uk.ac.ebi.eva.evaseqcol.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColExtendedDataFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component("NCBISeqColDataSource")
public class NCBISeqColDataSource implements SeqColDataSource {

    private final Logger logger = LoggerFactory.getLogger(NCBISeqColDataSource.class);

    private final NCBIAssemblyDataSource assemblyDataSource;

    private final NCBIAssemblySequenceDataSource assemblySequenceDataSource;

    private final SeqColExtendedDataFactory extendedDataFactory;

    public NCBISeqColDataSource(NCBIAssemblyDataSource assemblyDataSource,
                                NCBIAssemblySequenceDataSource assemblySequenceDataSource,
                                SeqColExtendedDataFactory extendedDataFactory) {
        this.assemblyDataSource = assemblyDataSource;
        this.assemblySequenceDataSource = assemblySequenceDataSource;
        this.extendedDataFactory = extendedDataFactory;
    }

    /**
     * Download both the Assembly Report and the Sequences FASTA file for the given accession and return the seqCol
     * data with the sequences' names in all the naming conventions found in the assembly report.
     */
    @Override
    public Optional<SeqColSourceData> getAllPossibleSeqColExtendedData(String accession) throws IOException {
        // Fetching Assembly Entity (Assembly Report)
        Optional<AssemblyEntity> assemblyEntity = assemblyDataSource.getAssemblyByAccession(accession);
        if (assemblyEntity.isEmpty()) {
            logger.error("Could not fetch Assembly Report from NCBI for assembly accession: {}", accession);
            return Optional.empty();
        } else if (assemblyEntity.get().getChromosomes() == null || assemblyEntity.get().getChromosomes().isEmpty()) {
            logger.error("No chromosome in assembly {}. Aborting", accession);
            return Optional.empty();
        }

        // Fetching Sequence Entity (FASTA File)
        Optional<AssemblySequenceEntity> sequenceEntity = assemblySequenceDataSource.getAssemblySequencesByAccession(accession);
        if (sequenceEntity.isEmpty()) {
            logger.error("Could not fetch Sequences FASTA file from NCBI for assembly accession: {}", accession);
            return Optional.empty();
        }
        logger.info("Assembly report and FASTA file have been fetched and parsed successfully");

        return Optional.of(new SeqColSourceData(
                extendedDataFactory.sequences(sequenceEntity.get()),
                extendedDataFactory.md5Sequences(sequenceEntity.get()),
                extendedDataFactory.lengths(assemblyEntity.get()),
                extendedDataFactory.allPossibleNames(assemblyEntity.get())));
    }

    /**
     * Parse the given FASTA file content and return the seqCol data, using the FASTA headers as sequences' names
     * (TEST naming convention).
     */
    public Optional<SeqColSourceData> getAllPossibleSeqColExtendedData(String accession, String fastaFileContent)
            throws IOException {
        Optional<AssemblySequenceEntity> sequenceEntity =
                assemblySequenceDataSource.getAssemblySequencesByAccession(accession, fastaFileContent);
        if (sequenceEntity.isEmpty()) {
            logger.error("Could not parse FASTA file content for accession {}", accession);
            return Optional.empty();
        }
        logger.info("FASTA file have been parsed successfully");

        return Optional.of(new SeqColSourceData(
                extendedDataFactory.sequences(sequenceEntity.get()),
                extendedDataFactory.md5Sequences(sequenceEntity.get()),
                extendedDataFactory.lengths(sequenceEntity.get()),
                Collections.singletonList(extendedDataFactory.namesFromFastaHeaders(sequenceEntity.get()))));
    }
}
