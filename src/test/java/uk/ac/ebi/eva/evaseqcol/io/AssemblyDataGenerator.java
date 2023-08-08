package uk.ac.ebi.eva.evaseqcol.io;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblyReportReaderFactory;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class AssemblyDataGenerator {

    private final String REPORT_FILE_PATH = "src/test/resources/GCA_000146045.2_R64_assembly_report.txt";
    private final String SEQUENCES_FILE_PATH = "src/test/resources/GCA_000146045.2_genome_sequence.fna";

    private static final String GCA_ACCESSION = "GCA_000146045.2";

    private static InputStreamReader sequencesStreamReader;
    private static InputStream sequencesStream;

    private static InputStreamReader reportStreamReader;
    private static InputStream reportStream;

    @Autowired
    private NCBIAssemblyReportReaderFactory reportReaderFactory;
    private NCBIAssemblyReportReader reportReader;

    @Autowired
    private NCBIAssemblySequenceReaderFactory sequenceReaderFactory;
    private NCBIAssemblySequenceReader sequenceReader;

    /**
     * Return the Assembly entity that corresponds to the assembly report located
     * under REPORT_FILE_PATH (see variable content above) having as assembly accession: "GCA_000146045.2"*/
    public AssemblyEntity generateAssemblyEntity() throws IOException {
        reportStream = new FileInputStream(
                new File(REPORT_FILE_PATH));
        reportStreamReader = new InputStreamReader(reportStream);
        reportReader = reportReaderFactory.build(reportStreamReader);
        return reportReader.getAssemblyEntity();
    }

    /**
     * Return the AssemblySequenceEntity that corresponds to the sequences FASTA file
     * located under SEQUENCES_FILE_PATH (see variable content above) having as assembly accession: "GCA_000146045.2"*/
    public AssemblySequenceEntity generateAssemblySequenceEntity() throws IOException {
        sequencesStream = new FileInputStream(
                new File(SEQUENCES_FILE_PATH));
        sequencesStreamReader = new InputStreamReader(sequencesStream);
        sequenceReader = sequenceReaderFactory.build(sequencesStreamReader, GCA_ACCESSION);
        return sequenceReader.getAssemblySequencesEntity();
    }
}
