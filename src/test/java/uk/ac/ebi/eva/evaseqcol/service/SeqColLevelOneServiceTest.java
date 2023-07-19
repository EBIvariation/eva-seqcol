package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelOne;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColLevelOneServiceTest {

    private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";
    private static final String GCA_ACCESSION = "GCA_000146045.2";
    //private final String REPORT_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_Dpse_3.0_assembly_report.txt";
    //private final String SEQUENCES_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_genome_sequence.fna";

    //private static final String GCF_ACCESSION = "GCF_000001765.3";
    private final boolean isScaffoldsEnabled = true;
    private AssemblyEntity assemblyEntity;
    private AssemblySequenceEntity assemblySequenceEntity;
    private static BufferedReader sequencesReader;
    private static InputStreamReader streamReaderSequences;
    private static InputStream streamSequences;

    private static BufferedReader reportReader;
    private static InputStreamReader streamReaderReport;
    private static InputStream streamReport;

    @Autowired
    private SeqColExtendedDataService seqColExtendedDataService;

    @Autowired
    private SeqColLevelOneService levelOneService;

    private DigestCalculator digestCalculator;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        streamSequences = new FileInputStream(
                new File(SEQUENCES_FILE_PATH_1));
        streamReaderSequences = new InputStreamReader(streamSequences);
        sequencesReader = new BufferedReader(streamReaderSequences);
        assemblySequenceEntity = new AssemblySequenceEntity()
                .setInsdcAccession(GCA_ACCESSION);

        streamReport = new FileInputStream(
                new File(REPORT_FILE_PATH_1));
        streamReaderReport = new InputStreamReader(streamReport);
        reportReader = new BufferedReader(streamReaderReport);
        digestCalculator = new DigestCalculator();
    }

    @AfterEach
    void tearDown() throws IOException {
        streamReport.close();
        streamReaderReport.close();
        streamSequences.close();
        streamReaderSequences.close();
    }

    void parseFile() throws IOException, NullPointerException {
        if (sequencesReader == null){
            throw new NullPointerException("Cannot use AssemblySequenceReader without having a valid InputStreamReader.");
        }
        ChecksumCalculator md5Calculator = new MD5Calculator();
        if (assemblySequenceEntity == null){
            assemblySequenceEntity = new AssemblySequenceEntity();
        }
        // Setting the accession of the whole assembly file
        assemblySequenceEntity.setInsdcAccession(GCA_ACCESSION);
        List<SeqColSequenceEntity> sequences = new LinkedList<>();
        String line = sequencesReader.readLine();
        while (line != null){
            if (line.startsWith(">")){
                SeqColSequenceEntity sequence = new SeqColSequenceEntity();
                String refSeq = line.substring(1, line.indexOf(' '));
                sequence.setRefseq(refSeq);
                line = sequencesReader.readLine();
                StringBuilder sequenceValue = new StringBuilder();
                while (line != null && !line.startsWith(">")){
                    // Looking for the sequence lines for this refseq
                    sequenceValue.append(line);
                    line = sequencesReader.readLine();
                }
                String md5checksum = md5Calculator.calculateChecksum(sequenceValue.toString().toUpperCase());
                sequence.setSequenceMD5(md5checksum);
                sequences.add(sequence);
            }
        }
        assemblySequenceEntity.setSequences(sequences);
    }

    void parseReport() throws IOException, NullPointerException {
        if (reportReader == null) {
            throw new NullPointerException("Cannot use AssemblyReportReader without having a valid InputStreamReader.");
        }
        String line = reportReader.readLine();
        while (line != null) {
            if (line.startsWith("# ")) {
                if (assemblyEntity == null) {
                    assemblyEntity = new AssemblyEntity();
                }
                parseAssemblyData(line);
            } else if (!line.startsWith("#")) {
                String[] columns = line.split("\t", -1);
                if (columns.length >= 6 && (columns[5].equals("=") || columns[5].equals("<>")) &&
                        (columns[4] != null && !columns[4].isEmpty() && !columns[4].equals("na"))) {
                    if (columns[3].equals("Chromosome") && columns[1].equals("assembled-molecule")) {
                        parseChromosomeLine(columns);
                    } else if (isScaffoldsEnabled) {
                        parseScaffoldLine(columns);
                    }
                }
            }
            line = reportReader.readLine();
        }
    }

    void parseAssemblyData(String line) {
        int tagEnd = line.indexOf(':');
        if (tagEnd == -1) {
            return;
        }
        String tag = line.substring(2, tagEnd);
        String tagData = line.substring(tagEnd + 1).trim();
        switch (tag) {
            case "Assembly name": {
                assemblyEntity.setName(tagData);
                break;
            }
            case "Organism name": {
                assemblyEntity.setOrganism(tagData);
                break;
            }
            case "Taxid": {
                assemblyEntity.setTaxid(Long.parseLong(tagData));
                break;
            }
            case "GenBank assembly accession": {
                assemblyEntity.setInsdcAccession(tagData);
                break;
            }
            case "RefSeq assembly accession": {
                assemblyEntity.setRefseq(tagData);
                break;
            }
            case "RefSeq assembly and GenBank assemblies identical": {
                assemblyEntity.setGenbankRefseqIdentical(tagData.equals("yes"));
                break;
            }
        }
    }

    void parseChromosomeLine(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setGenbankSequenceName(columns[0]);
        chromosomeEntity.setInsdcAccession(columns[4]);
        if (columns[6] == null || columns[6].isEmpty() || columns[6].equals("na")) {
            chromosomeEntity.setRefseq(null);
        } else {
            chromosomeEntity.setRefseq(columns[6]);
        }

        if (columns.length > 8) {
            try {
                Long seqLength = Long.parseLong(columns[8]);
                chromosomeEntity.setSeqLength(seqLength);
            } catch (NumberFormatException nfe) {

            }
        }

        if (columns.length > 9 && !columns[9].equals("na")) {
            chromosomeEntity.setUcscName(columns[9]);
        }

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        chromosomeEntity.setAssembly(this.assemblyEntity);
        chromosomeEntity.setContigType(SequenceEntity.ContigType.CHROMOSOME);

        List<ChromosomeEntity> chromosomes = this.assemblyEntity.getChromosomes();
        if (chromosomes == null) {
            chromosomes = new LinkedList<>();
            assemblyEntity.setChromosomes(chromosomes);
        }
        chromosomes.add(chromosomeEntity);
    }

    void parseScaffoldLine(String[] columns) {
        ChromosomeEntity scaffoldEntity = new ChromosomeEntity();

        scaffoldEntity.setGenbankSequenceName(columns[0]);
        scaffoldEntity.setInsdcAccession(columns[4]);
        if (columns[6] == null || columns[6].isEmpty() || columns[6].equals("na")) {
            scaffoldEntity.setRefseq(null);
        } else {
            scaffoldEntity.setRefseq(columns[6]);
        }

        if (columns.length > 8) {
            try {
                Long seqLength = Long.parseLong(columns[8]);
                scaffoldEntity.setSeqLength(seqLength);
            } catch (NumberFormatException nfe) {

            }
        }


        if (columns.length >= 10) {
            String ucscName = columns[9];
            if (!ucscName.equals("na")) {
                scaffoldEntity.setUcscName(ucscName);
            }
        }

        if (assemblyEntity == null) {
            assemblyEntity = new AssemblyEntity();
        }
        scaffoldEntity.setAssembly(this.assemblyEntity);
        scaffoldEntity.setContigType(SequenceEntity.ContigType.SCAFFOLD);

        List<ChromosomeEntity> scaffolds = this.assemblyEntity.getChromosomes();
        if (scaffolds == null) {
            scaffolds = new LinkedList<>();
            assemblyEntity.setChromosomes(scaffolds);
        }
        scaffolds.add(scaffoldEntity);
    }


    /**
     * Construct a seqCol level 1 entity out of three seqCol level 2 entities that
     * hold names, lengths and sequences objects*/
    SeqColLevelOneEntity constructSeqColLevelOne(List<SeqColExtendedDataEntity> extendedDataEntities,
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
        levelOneEntity.setSeqColLevel1Object(jsonLevelOne);
        String digest0 = digestCalculator.getSha512Digest(levelOneEntity.toString());
        levelOneEntity.setDigest(digest0);
        levelOneEntity.setNamingConvention(convention);
        return levelOneEntity;
    }

    @Test
    void addSequenceCollectionL1() throws IOException {
        parseReport();
        parseFile();
        List<SeqColExtendedDataEntity> extendedDataEntities = seqColExtendedDataService.constructExtendedSeqColDataList(
                assemblyEntity, assemblySequenceEntity, SeqColEntity.NamingConvention.GENBANK, GCA_ACCESSION
        ); // Contains the list of names, lengths and sequences exploded

        SeqColLevelOneEntity levelOneEntity = constructSeqColLevelOne(extendedDataEntities, SeqColEntity.NamingConvention.GENBANK);
        Optional<SeqColLevelOneEntity> savedEntity = levelOneService.addSequenceCollectionL1(levelOneEntity);
        assertTrue(savedEntity.isPresent());
        System.out.println(savedEntity.get());
    }
}