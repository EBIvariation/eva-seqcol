package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequencesEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

public abstract class AssemblySequencesReader {
    protected final BufferedReader reader;

    protected final String accession;

    protected AssemblySequencesEntity assemblySequencesEntity;


    protected boolean fileParsed = false;


    public AssemblySequencesReader(InputStreamReader inputStreamReader, String accession){
        this.reader = new BufferedReader(inputStreamReader);
        this.accession = accession;
    }

    public AssemblySequencesEntity getAssemblySequenceEntity() throws IOException, NoSuchAlgorithmException {
        if(!fileParsed || assemblySequencesEntity == null){
            parseFile();
        }
        return assemblySequencesEntity;
    }

    protected abstract void parseFile() throws IOException, NullPointerException, NoSuchAlgorithmException;

    public boolean ready() throws IOException {
        return reader.ready();
    }
}
