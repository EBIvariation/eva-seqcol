package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.evaseqcol.datasource.NCBISeqColDataSource;
import uk.ac.ebi.eva.evaseqcol.datasource.SeqColSourceData;
import uk.ac.ebi.eva.evaseqcol.dto.IngestionResultEntity;
import uk.ac.ebi.eva.evaseqcol.dto.InsertedSeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyAlreadyIngestedException;
import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Creates seqCol objects from external sources (NCBI assembly or FASTA file content) and stores them.
 * Each seqCol object is stored in its own transaction (see {@link SeqColService#addFullSequenceCollection}).
 */
@Service
public class SeqColIngestionService {

    private final Logger logger = LoggerFactory.getLogger(SeqColIngestionService.class);

    private final NCBISeqColDataSource ncbiSeqColDataSource;

    private final SeqColService seqColService;

    private final SeqColLevelOneService levelOneService;

    private final SeqColExtendedDataFactory extendedDataFactory;

    private final SeqColLevelOneFactory levelOneFactory;

    public SeqColIngestionService(NCBISeqColDataSource ncbiSeqColDataSource, SeqColService seqColService,
                                  SeqColLevelOneService levelOneService, SeqColExtendedDataFactory extendedDataFactory,
                                  SeqColLevelOneFactory levelOneFactory) {
        this.ncbiSeqColDataSource = ncbiSeqColDataSource;
        this.seqColService = seqColService;
        this.levelOneService = levelOneService;
        this.extendedDataFactory = extendedDataFactory;
        this.levelOneFactory = levelOneFactory;
    }

    /**
     * Fetch and insert all possible seqCol objects for the given assembly accession.
     * NOTE: All possible seqCol objects means with all possible/provided naming conventions that could be found in the
     * assembly report.
     * @return the level 0 digests of the inserted seqcol objects
     */
    public IngestionResultEntity fetchAndInsertAllSeqColByAssemblyAccession(String assemblyAccession)
            throws IOException {
        if (levelOneService.existsBySourceId(assemblyAccession)) {
            logger.warn("Seqcol objects for assembly {} have been already ingested... Nothing to ingest !",
                        assemblyAccession);
            throw new AssemblyAlreadyIngestedException(assemblyAccession);
        }
        return insertAll(ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(assemblyAccession), assemblyAccession);
    }

    /**
     * Parse the FASTA file content and insert the corresponding seqCol object (TEST naming convention)
     */
    public IngestionResultEntity fetchAndInsertAllSeqColInFastaFile(String accession, String fastaFileContent)
            throws IOException {
        return insertAll(ncbiSeqColDataSource.getAllPossibleSeqColExtendedData(accession, fastaFileContent), accession);
    }

    private IngestionResultEntity insertAll(Optional<SeqColSourceData> optionalSourceData, String accession)
            throws IOException {
        SeqColSourceData sourceData = optionalSourceData.orElseThrow(() -> {
            logger.warn("No seqCol data corresponding to accession {} could be found", accession);
            return new AssemblyNotFoundException(accession);
        });

        IngestionResultEntity ingestionResult = new IngestionResultEntity();
        ingestionResult.setAssemblyAccession(accession);
        for (SeqColExtendedDataEntity<List<String>> names : sourceData.namesPerNamingConvention()) {
            SeqColExtendedDataSet extendedData = extendedDataFactory.extendedDataSet(
                    sourceData.sequences(), sourceData.md5Sequences(), names, sourceData.lengths());
            SeqColLevelOneEntity levelOneEntity = levelOneFactory.create(extendedData, names.getNamingConvention(),
                                                                         accession);
            try {
                String digest = seqColService.addFullSequenceCollection(levelOneEntity, extendedData);
                logger.info("Successfully inserted seqCol for accession {} with naming convention {}",
                            accession, names.getNamingConvention());
                ingestionResult.addInsertedSeqCol(new InsertedSeqColEntity(digest,
                                                                           names.getNamingConvention().toString()));
                ingestionResult.incrementNumberOfInsertedSeqCols();
            } catch (DuplicateSeqColException e) {
                logger.info("Seqcol for {} and naming convention {} already exists. Skipping.",
                            accession, names.getNamingConvention());
            }
        }
        if (ingestionResult.getNumberOfInsertedSeqcols() == 0) {
            logger.warn("Seqcol objects for assembly {} have been already ingested", accession);
            throw new AssemblyAlreadyIngestedException(accession);
        }
        return ingestionResult;
    }
}
