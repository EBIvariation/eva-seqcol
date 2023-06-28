package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.utils.MD5Digest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public class NCBIAssemblySequencesReader extends AssemblySequencesReader{
    public NCBIAssemblySequencesReader(InputStreamReader inputStreamReader, String accession){
        super(inputStreamReader, accession);
    }

    @Override
    protected void parseFile() throws IOException, NullPointerException, NoSuchAlgorithmException {
        if (reader == null){
            throw new NullPointerException("Cannot use AssemblySequenceReader without having a valid InputStreamReader.");
        }
        MD5Digest md5Digest = new MD5Digest();
        if (assemblySequencesEntity == null){
            assemblySequencesEntity = new AssemblySequencesEntity();
        }
        // Setting the accession of the whole assembly file
        assemblySequencesEntity.setInsdcAccession(accession);
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
                String md5checksum = md5Digest.hash(sequenceValue.toString());
                sequence.setSequenceMD5(md5checksum);
                sequences.add(sequence);
            }
        }
        assemblySequencesEntity.setSequences(sequences);
        fileParsed = true;
        reader.close();
    }

}
