package uk.ac.ebi.eva.evaseqcol.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColExtendedDataService;
import uk.ac.ebi.eva.evaseqcol.service.SeqColLevelOneService;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is responsible for saving and deleting seqCol
 * objects to and from the database.*/
@Component
public class SeqColWriter {

    private static final Logger logger = LoggerFactory.getLogger(SeqColWriter.class);

    private List<SeqColExtendedDataEntity> extendedDataEntitiesUcsc;

    private List<SeqColExtendedDataEntity> extendedDataEntitiesGenbank;

    private SeqColLevelOneEntity levelOneEntityUcsc;

    private SeqColLevelOneEntity levelOneEntityGenbank;

    private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";

    private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";

    private static final String GCA_ACCESSION = "GCA_000146045.2";

    private List<String> insertedSeqColDigests; // Holds the digests of the seqCol objects inserted by this class

    private static InputStreamReader sequencesStreamReader;

    private static InputStream sequencesStream;

    private static InputStreamReader reportStreamReader;

    private static InputStream reportStream;


    private NCBIAssemblyReportReaderFactory reportReaderFactory;

    private NCBIAssemblyReportReader reportReader;


    private NCBIAssemblySequenceReaderFactory sequenceReaderFactory;

    private NCBIAssemblySequenceReader sequenceReader;


    private SeqColLevelOneService levelOneService;


    private SeqColExtendedDataService extendedDataService;


    private SeqColService seqColService;

    @Autowired
    public SeqColWriter(NCBIAssemblyReportReaderFactory reportReaderFactory, NCBIAssemblySequenceReaderFactory sequenceReaderFactory,
                        SeqColLevelOneService levelOneService, SeqColExtendedDataService extendedDataService, SeqColService seqColService) {
        this.reportReaderFactory = reportReaderFactory;
        this.sequenceReaderFactory = sequenceReaderFactory;
        this.levelOneService = levelOneService;
        this.extendedDataService = extendedDataService;
        this.seqColService = seqColService;
    }

    /**
     * Setup the report reader (assembly report reader),
     * the sequences' reader (assembly FASTA file reader)
     * and other necessary objects.
     * */
    private void setUp() throws FileNotFoundException {
        reportStream = new FileInputStream(
                new File(REPORT_FILE_PATH_1));
        reportStreamReader = new InputStreamReader(reportStream);
        reportReader = reportReaderFactory.build(reportStreamReader);

        sequencesStream = new FileInputStream(
                new File(SEQUENCES_FILE_PATH_1));
        sequencesStreamReader = new InputStreamReader(sequencesStream);
        sequenceReader = sequenceReaderFactory.build(sequencesStreamReader, GCA_ACCESSION);

        insertedSeqColDigests = new ArrayList<>();
    }

    /**
     * Close streams and streams readers*/
    private void tearDown() throws IOException {
        reportStream.close();
        reportStreamReader.close();
        sequencesStream.close();
        sequencesStreamReader.close();
    }

    /**
     * Save seqCol objects of assembly GCA_ACCESSION (see variable content above) for naming conventions UCSC and GENBANK
     * NOTE: The assembly report and the sequences FASTA file for this assembly are already downloaded
     * and put into "src/test/resources/"
     * */
    public void write() throws IOException {
        setUp();
        AssemblyEntity assemblyEntity = reportReader.getAssemblyEntity();
        AssemblySequenceEntity assemblySequenceEntity = sequenceReader.getAssemblySequencesEntity();

        // Insert seqCol for UCSC naming convention
        extendedDataEntitiesUcsc = extendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.UCSC
        );
        levelOneEntityUcsc = levelOneService.constructSeqColLevelOne(
                extendedDataEntitiesUcsc, SeqColEntity.NamingConvention.UCSC);
        Optional<String> resultDigestUcsc = seqColService.addFullSequenceCollection(levelOneEntityUcsc, extendedDataEntitiesUcsc);
        if (resultDigestUcsc.isPresent()) {
            logger.info("Successfully inserted seqCol object with the assembly accession " + GCA_ACCESSION + " for " +
                                "naming convention " + SeqColEntity.NamingConvention.UCSC);
        } else {
            logger.error("Could not insert seqCol object with the assembly accession " + GCA_ACCESSION + " for " +
                                 "naming convention " + SeqColEntity.NamingConvention.UCSC);
        }
        insertedSeqColDigests.add(resultDigestUcsc.get());

        // Insert seqCol for GENBANK naming convention
        extendedDataEntitiesGenbank = extendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK
        );
        levelOneEntityGenbank = levelOneService.constructSeqColLevelOne(
                extendedDataEntitiesGenbank, SeqColEntity.NamingConvention.GENBANK);
        Optional<String> resultDigestGenbank = seqColService.addFullSequenceCollection(levelOneEntityGenbank, extendedDataEntitiesGenbank);
        if (resultDigestGenbank.isPresent()) {
            logger.info("Successfully inserted seqCol object with the assembly accession " + GCA_ACCESSION + " for " +
                                "naming convention " + SeqColEntity.NamingConvention.GENBANK);
        } else {
            logger.error("Could not insert seqCol object with the assembly accession " + GCA_ACCESSION + " for " +
                                 "naming convention " + SeqColEntity.NamingConvention.GENBANK);
        }
        insertedSeqColDigests.add(resultDigestGenbank.get());

        // Clear streams
        tearDown();
    }

    /**
     * Remove all inserted seqCol objects from the database.
     * */
    public void clearData() {
        seqColService.removeAllSeqCol();
    }

}
