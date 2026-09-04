package uk.ac.ebi.eva.evaseqcol.dus;

import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.IncorrectAccessionException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class NCBIBrowser {
    public static final String NCBI_SERVER = "https://ftp.ncbi.nlm.nih.gov";

    public static final String PATH_GENOMES_ALL = "/genomes/all/";

    private final HttpFileBrowser browser;

    public NCBIBrowser() {
        this.browser = new HttpFileBrowser();
    }

    /**
     * Takes a Genbank or Refseq accession and converts it to the equivalent path used by NCBI's server.
     * For example, on input "GCF_007608995.1" the output path is "/genomes/all/GCF/007/608/995/GCF_007608995
     * .1_ASM760899v1/".
     *
     * @param accession Any GCA or GCF String
     * @return Path relative to ftp.ncbi.nlm.nih.gov
     * @throws IOException Passes exception thrown while listing the remote directory
     */
    public Optional<String> getGenomeReportDirectory(String accession) throws IOException, IllegalArgumentException {

        if (accession.length() < 15) {
            throw new IncorrectAccessionException("Accession should be at least 15 characters long!");
        }

        //GCA_004051055.1
        String rawQuery = accession;
        String path = "";

        // path = "GCA/"
        path += accession.substring(0, 3) + "/";
        // accession = "004051055.1"
        accession = accession.substring(4);

        // path = "GCA/004/"
        path += accession.substring(0, 3) + "/";
        // accession = "051055.1"
        accession = accession.substring(3);

        // path = "GCA/004/051/"
        path += accession.substring(0, 3) + "/";
        // accession = "055.1"
        accession = accession.substring(3);

        // path = "GCA/004/051/055/"
        path += accession.substring(0, 3) + "/";

        String currPath = PATH_GENOMES_ALL + path;
        List<String> entries = browser.listDirectory(NCBI_SERVER + currPath);

        // We're assuming that the directory will always have a suffix starting with an underscore GCA_004051055.1_
        Optional<String> dir = entries.stream()
                                       .filter(name -> name.startsWith(rawQuery + "_") && name.endsWith("/"))
                                       .findFirst();
        if (dir.isPresent()) {
            // path = "GCA/004/051/055/GCA_004051055.1_ASM405105v1/"
            return Optional.of(currPath + dir.get());
        }

        return Optional.empty();
    }

    /**
     * @param directoryPath The path of the directory in which target report is located relative to root of the
     *                      server. Eg:- "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/"
     * @return An InputStream of the first *assembly_report.txt file it finds.
     * @throws IOException Passes exception thrown while listing or fetching the remote directory
     */
    public InputStream getAssemblyReportInputStream(String directoryPath) throws IOException {
        String reportName = findAssemblyReportName(directoryPath);
        return browser.openStream(NCBI_SERVER + directoryPath + reportName);
    }

    public RemoteFile getNCBIAssemblyReportFile(String directoryPath) throws IOException {
        String reportName = findAssemblyReportName(directoryPath);
        long size = browser.headContentLength(NCBI_SERVER + directoryPath + reportName);
        return new RemoteFile(reportName, size);
    }

    /**
     * Return a pointer to the assembly sequences' FASTA file that will be downloaded.
     * When searching for the FASTA file, it may occur that we find multiple files
     * that has their names end either with "_genomic.fna.gz" or with "_from_genomic.fna.gz". In
     * this cas we'll assume that the one that ends with "_genomic.fna.gz" is the one
     * we're looking for. Hence, comes the "from" in the filter
     * @see <a href="https://ftp.ncbi.nlm.nih.gov/genomes/all/GCF/000/001/405/GCF_000001405.40_GRCh38.p14/">Example of
     * multiple FASTA files for the same assmebly</a>*/
    public RemoteFile getAssemblySequencesFastaFile(String directoryPath) throws IOException {
        List<String> entries = browser.listDirectory(NCBI_SERVER + directoryPath);
        String name = entries.stream()
                              .filter(entry -> entry.contains("genomic.fna.gz") && !entry.contains("from"))
                              .findFirst()
                              .orElseThrow(() -> new AssemblyNotFoundException(
                                      "Assembly FASTA file not present in given directory: " + directoryPath));
        long size = browser.headContentLength(NCBI_SERVER + directoryPath + name);
        return new RemoteFile(name, size);
    }

    public boolean downloadFile(String filePath, Path downloadFilePath, long expectedSize) throws IOException {
        return browser.downloadFile(NCBI_SERVER + filePath, downloadFilePath, expectedSize);
    }

    private String findAssemblyReportName(String directoryPath) throws IOException {
        List<String> entries = browser.listDirectory(NCBI_SERVER + directoryPath);
        return entries.stream()
                       .filter(name -> name.contains("assembly_report.txt"))
                       .findFirst()
                       .orElseThrow(() -> new AssemblyNotFoundException(
                               "Assembly Report File not present in given directory: " + directoryPath));
    }

}
