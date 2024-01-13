package uk.ac.ebi.eva.evaseqcol.exception;

public class AssemblyAlreadyIngestedException extends RuntimeException{
    public AssemblyAlreadyIngestedException(String assemblyAccession) {
        super("Seqcol objects for assembly " + assemblyAccession + " has been already ingested");
    }
}
