package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

public abstract class AssemblySequenceReader {
    protected final BufferedReader reader;

    protected final String accession;

    protected AssemblySequenceEntity assemblySequenceEntity;


    protected boolean fileParsed = false;


    public AssemblySequenceReader(InputStreamReader inputStreamReader, String accession){
        this.reader = new BufferedReader(inputStreamReader);
        this.accession = accession;
    }

    public AssemblySequenceEntity getAssemblySequencesEntity() throws IOException {
        if(!fileParsed || assemblySequenceEntity == null){
            parseFile();
        }
        return assemblySequenceEntity;
    }

    // TODO: provide a method here that will call parseFile in the inheritees (a method prone to exceptions)
    //  and close the reader after parseFile exits.

    protected abstract void parseFile() throws IOException, NullPointerException;

    public boolean ready() throws IOException {
        return reader.ready();
    }
}
