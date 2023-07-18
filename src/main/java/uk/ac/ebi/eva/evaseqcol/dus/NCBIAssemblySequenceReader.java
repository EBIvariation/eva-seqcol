package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.refget.MD5Calculator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class NCBIAssemblySequenceReader extends AssemblySequenceReader {
    public NCBIAssemblySequenceReader(InputStreamReader inputStreamReader, String accession){
        super(inputStreamReader, accession);
    }

    @Override
    protected void parseFile() throws IOException, NullPointerException {
        if (reader == null){
            throw new NullPointerException("Cannot use AssemblySequenceReader without having a valid InputStreamReader.");
        }
        ChecksumCalculator md5Calculator = new MD5Calculator();
        if (assemblySequenceEntity == null){
            assemblySequenceEntity = new AssemblySequenceEntity();
        }
        // Setting the accession of the whole assembly file
        assemblySequenceEntity.setInsdcAccession(accession);
        List<SeqColSequenceEntity> sequences = new LinkedList<>();
        String line = reader.readLine();
        while (line != null){
            if (line.startsWith(">")){
                SeqColSequenceEntity sequence = new SeqColSequenceEntity();
                String refSeq = line.substring(1, line.indexOf(' '));
                sequence.setRefseq(refSeq);
                line = reader.readLine();
                StringBuilder sequenceValue = new StringBuilder();
                while (line != null && !line.startsWith(">")){
                    // Looking for the sequence lines for this refseq
                    sequenceValue.append(line);
                    line = reader.readLine();
                }
                String md5checksum = md5Calculator.calculateChecksum(sequenceValue.toString().toUpperCase());
                sequence.setSequenceMD5(md5checksum);
                sequences.add(sequence);
            }
        }
        assemblySequenceEntity.setSequences(sequences);
        fileParsed = true;
        reader.close();
    }

    /**
     * Normalize the given sequence following the
     * */
    String calculateChecksum(String sequence) {
        return "";
    }

}
