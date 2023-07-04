package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AssemblyReportReader {

    protected final BufferedReader reader;

    protected AssemblyEntity assemblyEntity;

    protected final boolean isScaffoldsEnabled;

    protected boolean reportParsed = false;

    public AssemblyReportReader(InputStreamReader inputStreamReader, boolean isScaffoldsEnabled) {
        this.reader = new BufferedReader(inputStreamReader);
        this.isScaffoldsEnabled = isScaffoldsEnabled;
    }

    /**
     * Returns the class-level instance variable of {@link AssemblyEntity}. If the variable has not been initialized
     * or the assembly report has not been parsed yet, it calls {@link #parseReport()} and then returns the variable
     * that would have been initialized by {@link #parseReport()}.
     *
     * @return {@link AssemblyEntity} containing all metadata extracted from the report along with a list of
     * {@link ChromosomeEntity} which also contain their own metadata.
     * @throws IOException Passes IOException thrown by {@link #parseReport()}
     */
    public AssemblyEntity getAssemblyEntity() throws IOException {
        if (!reportParsed || assemblyEntity == null) {
            parseReport();
        }
        return assemblyEntity;
    }

    /**
     * Reads the report line-by-line and calls the relevant methods to parse each line based on its starting characters.
     *
     * @throws IOException Passes IOException thrown by {@link BufferedReader#readLine()}
     */
    protected abstract void parseReport() throws IOException, NullPointerException;

    /**
     * Parses lines in assembly report containing Assembly metadata. Breaks line into a tag:tagData format and
     * sets tagData in {@link AssemblyEntity} fields corresponding to the tag found in given line.
     *
     * @param line A line of assembly report file starting with "# ".
     */
    protected abstract void parseAssemblyData(String line);

    /**
     * Parses lines in assembly report containing Chromosome metadata. This array is used to set metadata to
     * corresponding fields in {@link ChromosomeEntity}.
     *
     * @param columns An array of fields in a line of the assembly report file not starting with "#".
     */
    protected abstract void parseChromosomeLine(String[] columns);

    /**
     * Parses lines in assembly report containing Scaffold metadata. This array is used to set metadata to corresponding
     * fields in {@link ChromosomeEntity}.
     *
     * @param columns An array of fields in a line of the assembly report file not starting with "#".
     */
    protected abstract void parseScaffoldLine(String[] columns);

    public boolean ready() throws IOException {
        return reader.ready();
    }
}
