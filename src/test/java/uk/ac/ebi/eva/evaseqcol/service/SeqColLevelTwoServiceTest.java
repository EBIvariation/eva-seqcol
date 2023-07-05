package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelTwo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColLevelTwoServiceTest {


    //private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    //private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";
    private final String REPORT_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_Dpse_3.0_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_genome_sequence.fna";

    //private static final String GCA_ACCESSION = "GCA_000146045.2";
    private static final String GCF_ACCESSION = "GCF_000001765.3";
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
    private SeqColLevelTwoService levelTwoService;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        streamSequences = new FileInputStream(
                new File(SEQUENCES_FILE_PATH_2));
        streamReaderSequences = new InputStreamReader(streamSequences);
        sequencesReader = new BufferedReader(streamReaderSequences);
        assemblySequenceEntity = new AssemblySequenceEntity()
                .setInsdcAccession(GCF_ACCESSION);

        streamReport = new FileInputStream(
                new File(REPORT_FILE_PATH_2));
        streamReaderReport = new InputStreamReader(streamReport);
        reportReader = new BufferedReader(streamReaderReport);
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
        assemblySequenceEntity.setInsdcAccession(GCF_ACCESSION);
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


    @Test
    void getAssemblyReportReader() throws IOException {
        System.out.println("READY REPORT 2 ?" + reportReader.ready());
        assertTrue(reportReader.ready());
    }

    @Test
    void getAssemblySequencesReader() throws IOException {
        assertTrue(sequencesReader.ready());
    }

    @Test
    void getAssemblyReportAndSequences() throws IOException {
        parseReport();
        assertNotNull(assemblyEntity);
        assertTrue(assemblyEntity.getChromosomes().size() > 0);
        parseFile();
        assertNotNull(assemblySequenceEntity);
        assertTrue(assemblySequenceEntity.getSequences().size() > 0);
        assertEquals(assemblyEntity.getChromosomes().size(), assemblySequenceEntity.getSequences().size());
    }


    /**
     * Return the 3 seqcol objects (names, lengths and sequences) of the given naming convention*/
    List<SeqColLevelTwoEntity> constructLevelTwoSeqCols(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                            SeqColEntity.NamingConvention convention){
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        sortReportAndSequencesByRefseq(assemblyEntity, assemblySequenceEntity, GCF_ACCESSION);
        SeqColLevelTwoEntity namesEntity;
        SeqColLevelTwoEntity lengthsEntity;
        SeqColLevelTwoEntity sequencesEntity;
        JSONLevelTwo jsonNamesObject = new JSONLevelTwo();
        JSONLevelTwo jsonLengthsObject = new JSONLevelTwo();
        JSONLevelTwo jsonSequencesObject = new JSONLevelTwo();
        List<String> sequencesNamesObject = new LinkedList<>(); // Array of sequences' names
        List<String> sequencesLengthsObject = new LinkedList<>(); // Array of sequences' lengths
        List<String> sequencesObject = new LinkedList<>(); // // Array of actual sequences



        // Setting the sequences' names
        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            switch (convention) {
                case ENA:
                    sequencesNamesObject.add(chromosome.getEnaSequenceName());
                    break;
                case GENBANK:
                    sequencesNamesObject.add(chromosome.getGenbankSequenceName());
                    break;
                case UCSC:
                    sequencesNamesObject.add(chromosome.getUcscName());
                    break;
            }
            sequencesLengthsObject.add(chromosome.getSeqLength().toString());
        }

        // Setting actual sequences
        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesObject.add(sequence.getSequenceMD5());
        }

        jsonNamesObject.setObject(sequencesNamesObject);
        String namesDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesNamesObject AND PUT IT HERE
        namesEntity = new SeqColLevelTwoEntity().setObject(jsonNamesObject);
        namesEntity.setDigest(namesDigest);

        jsonLengthsObject.setObject(sequencesLengthsObject);
        String lengthsDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesLengthsObject AND PUT IT HERE
        lengthsEntity = new SeqColLevelTwoEntity().setObject(jsonLengthsObject);
        lengthsEntity.setDigest(lengthsDigest);

        jsonSequencesObject.setObject(sequencesObject);
        String sequencesDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesObject AND PUT IT HERE
        sequencesEntity = new SeqColLevelTwoEntity().setObject(jsonSequencesObject);
        sequencesEntity.setDigest(sequencesDigest);

        List<SeqColLevelTwoEntity> entities = new ArrayList<>(
                Arrays.asList(namesEntity, lengthsEntity, sequencesEntity)
        );
        return entities;
    }

    /**
     * Return "GCF" (INSDC: GenBank) or "GCA" (Refseq) depending on the given accession */
    String getAccessionType(String accession) {
        if (accession.startsWith("GCA"))
            return "GCA"; // INSDC: GenBank accession
        else
            return "GCF"; // Refseq accession
    }

    void sortReportAndSequencesByRefseq(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                        String accession) {
        String accessionType = getAccessionType(accession);

        Comparator<ChromosomeEntity> chromosomeComparator = (o1, o2) -> {
            String identifier1 = new String();
            String identifier2 = new String();
            switch (accessionType) {
                case "GCF":
                    identifier1 = o1.getRefseq();
                    identifier2 = o2.getRefseq();
                    break;
                case "GCA":
                    identifier1 = o1.getInsdcAccession();
                    identifier2 = o2.getInsdcAccession();
                    break;
            }

            String substring1 = identifier1.substring(identifier1.indexOf(".") + 1, identifier1.length());
            String substring2 = identifier2.substring(identifier2.indexOf(".") + 1, identifier2.length());
            if (!substring1.equals(substring2))
                return substring1.compareTo(substring2);
            return identifier1.substring(0,identifier1.indexOf(".")).compareTo(identifier2.substring(0,identifier2.indexOf(".")));
        };
        Collections.sort(assemblyEntity.getChromosomes(), chromosomeComparator);
        Comparator<SeqColSequenceEntity> sequenceComparator = (o1, o2) -> {
            String identifier = o1.getRefseq();
            String identifier1 = o2.getRefseq();
            String substring1 = identifier.substring(identifier.indexOf(".") + 1, identifier.length());
            String substring2 = identifier1.substring(identifier1.indexOf(".") + 1, identifier1.length());
            if (!substring1.equals(substring2))
                return substring1.compareTo(substring2);
            return identifier.substring(0,identifier.indexOf(".")).compareTo(identifier1.substring(0,identifier1.indexOf(".")));
        };
        Collections.sort(sequenceEntity.getSequences(), sequenceComparator);
    }

    @Test
    void sortingSequencesAndChromosomesTest() throws IOException {
        parseReport();
        parseFile();
        assertNotNull(assemblyEntity);
        assertEquals(assemblyEntity.getChromosomes().size(), assemblySequenceEntity.getSequences().size());
        sortReportAndSequencesByRefseq(assemblyEntity, assemblySequenceEntity, GCF_ACCESSION);
        for (int i=0; i<assemblyEntity.getChromosomes().size(); i++) {
            assertEquals(assemblyEntity.getChromosomes().get(i).getRefseq(),
                         assemblySequenceEntity.getSequences().get(i).getRefseq());
        }

    }

    @Test
    /**
     * Adding multiple seqCol objects*/
    void addSequenceCollectionL2() throws IOException {
        parseReport();
        parseFile();
        assertNotNull(assemblyEntity);
        assertEquals(assemblyEntity.getChromosomes().size(), assemblySequenceEntity.getSequences().size());
        List<SeqColLevelTwoEntity> levelTwoEntities = constructLevelTwoSeqCols(assemblyEntity, assemblySequenceEntity,
                                                                               SeqColEntity.NamingConvention.GENBANK);
        List<SeqColLevelTwoEntity> fetchEntities = levelTwoService.addAll(levelTwoEntities);
        assertNotNull(fetchEntities);
        assertTrue(fetchEntities.size() > 0);
    }
}