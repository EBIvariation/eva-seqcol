package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class ENAAssemblyReportReader extends AssemblyReportReader{

    public ENAAssemblyReportReader(InputStreamReader inputStreamReader, boolean isScaffoldsEnabled) {
        super(inputStreamReader, isScaffoldsEnabled);
    }

    protected void parseReport() throws IOException, NullPointerException {
        if (reader == null) {
            throw new NullPointerException("Cannot use AssemblyReportReader without having a valid InputStreamReader.");
        }
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("accession")) {
                if (assemblyEntity == null) {
                    assemblyEntity = new AssemblyEntity();
                }
                parseAssemblyData(line);
            } else if (!line.startsWith("accession")) {
                String[] columns = line.split("\t", -1);
                if (columns.length >= 6) {
                    if (columns[5].equals("Chromosome") && columns[3].equals("assembled-molecule")) {
                        parseChromosomeLine(columns);
                    } else if (isScaffoldsEnabled) {
                        parseScaffoldLine(columns);
                    }
                }
            }
            line = reader.readLine();
        }
        reportParsed = true;
        reader.close();
    }

    // Not present in ENA assembly reports
    protected void parseAssemblyData(String line) {}

    protected void parseChromosomeLine(String[] columns) {
        ChromosomeEntity chromosomeEntity = new ChromosomeEntity();

        chromosomeEntity.setInsdcAccession(columns[0]);
        chromosomeEntity.setEnaSequenceName(columns[1]);

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

    protected void parseScaffoldLine(String[] columns) {
        ChromosomeEntity scaffoldEntity = new ChromosomeEntity();

        scaffoldEntity.setInsdcAccession(columns[0]);
        scaffoldEntity.setEnaSequenceName(columns[1]);

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
}
