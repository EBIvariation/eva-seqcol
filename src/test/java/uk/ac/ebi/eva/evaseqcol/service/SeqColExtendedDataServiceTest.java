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
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
class SeqColExtendedDataEntityServiceTest {


    private final String REPORT_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH_1 = "src/test/resources/GCA_000146045.2_genome_sequence.fna";
    //private final String REPORT_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_Dpse_3.0_assembly_report.txt";
    //private final String SEQUENCES_FILE_PATH_2 = "src/test/resources/GCF_000001765.3_genome_sequence.fna"; // Reduced to Only 9 sequences

    private static final String GCA_ACCESSION = "GCA_000146045.2";
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
    private SeqColExtendedDataService levelTwoService;

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
        assertEquals(assemblySequenceEntity.getSequences().size(), assemblyEntity.getChromosomes().size());
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

    /**
     * Return the 3 seqcol objects (names, lengths and sequences) of the given naming convention*/
    List<SeqColExtendedDataEntity> constructLevelTwoSeqCols(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                                            SeqColEntity.NamingConvention convention) throws IOException {
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        sortReportAndSequencesBySequenceIdentifier(assemblyEntity, sequenceEntity, GCA_ACCESSION);
        List<SeqColExtendedDataEntity> entities = new Li
    }

    /**
     * Return "GCF" (INSDC: GenBank) or "GCA" (Refseq) depending on the given accession */
    String getAccessionType(String accession) {
        if (accession.startsWith("GCA"))
            return "GCA"; // INSDC: GenBank accession
        else
            return "GCF"; // Refseq accession
    }

    void sortReportAndSequencesBySequenceIdentifier(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
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
        assertEquals(assemblySequenceEntity.getSequences().size(), assemblyEntity.getChromosomes().size());
        sortReportAndSequencesBySequenceIdentifier(assemblyEntity, assemblySequenceEntity, GCA_ACCESSION);
        for (int i=0; i<assemblySequenceEntity.getSequences().size(); i++) {
            assertEquals(assemblyEntity.getChromosomes().get(i).getInsdcAccession(),
                         assemblySequenceEntity.getSequences().get(i).getRefseq());
        }

    }

    @Test
    /**
     * Adding multiple seqCol extended data objects*/
    void addSeqColExtendedData() throws IOException {
        parseReport();
        parseFile();
        assertNotNull(assemblyEntity);
        assertEquals(assemblySequenceEntity.getSequences().size(), assemblyEntity.getChromosomes().size());
        //List<SeqColExtendedDataEntity> levelTwoEntities = constructLevelTwoSeqCols(assemblyEntity, assemblySequenceEntity,
                                                                                   //SeqColEntity.NamingConvention.GENBANK);
        //List<SeqColExtendedDataEntity> fetchEntities = levelTwoService.addAll(levelTwoEntities);
        //assertNotNull(fetchEntities);
        //assertTrue(fetchEntities.size() > 0);
        sortReportAndSequencesBySequenceIdentifier(assemblyEntity, assemblySequenceEntity, GCA_ACCESSION);
        SeqColExtendedDataEntity seqColNamesObject = constructSeqColLengthsObject(assemblyEntity);
        assertNotNull(seqColNamesObject);
        assertNotNull(seqColNamesObject.getDigest());
        System.out.println("DIGEST: " + seqColNamesObject.getDigest());
        for (int i=0; i<5; i++) {
            System.out.println(seqColNamesObject.getObject().getObject().get(i));
        }
    }
}