package uk.ac.ebi.eva.evaseqcol.dus;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.IncorrectAccessionException;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class NCBIBrowser extends PassiveAnonymousFTPClient{
    public static final String NCBI_FTP_SERVER = "ftp.ncbi.nlm.nih.gov";

    public static final String PATH_GENOMES_ALL = "/genomes/all/";

    private String ftpProxyHost;

    private Integer ftpProxyPort;

    public NCBIBrowser(String ftpProxyHost, Integer ftpProxyPort) {
        this.ftpProxyHost = ftpProxyHost;
        this.ftpProxyPort = ftpProxyPort;
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier=2))
    public void connect() throws IOException {
        if (ftpProxyHost != null && !ftpProxyHost.equals("null") &&
                ftpProxyPort != null && ftpProxyPort != 0) {
            super.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ftpProxyHost, ftpProxyPort)));
        }
        super.connect(NCBI_FTP_SERVER);
    }

    /**
     * Takes a Genbank or Refseq accession and converts it to the equivalent path used by NCBI's FTP server.
     * For example, on input "GCF_007608995.1" the output path is "/genomes/all/GCF/007/608/995/GCF_007608995
     * .1_ASM760899v1/".
     *
     * @param accession Any GCA or GCF String
     * @return Path relative to ftp.ncbi.nlm.nih.gov
     * @throws IOException Passes exception thrown by FTPBrowser.listDirectories()
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
        FTPFile[] ftpFiles = super.listFiles(currPath, FTPFileFilters.ALL);

        if (ftpFiles.length > 0) {
            // We're assuming that the directory will always have a suffix stating with an underscore GCA_004051055.1_
            Optional<FTPFile> dir = Arrays.stream(ftpFiles).filter(it -> it.getName().startsWith(rawQuery+"_")).findFirst();
            if (dir.isPresent()) {
                if (dir.get().isSymbolicLink()) {
                    // symbolic link relative to current path Optional
                    // symlink = "../../../../../archive/old_genbank/Eukaryotes/vertebrates_mammals/Homo_sapiens/GRCh37"
                    // path = "/genomes/archive/old_genbank/Eukaryotes/vertebrates_mammals/Homo_sapiens/GRCh37"
                    return Optional.of(Paths.get(currPath + dir.get().getLink()).normalize().toString() + "/");
                } else if (dir.get().isDirectory()) {
                    // path = "GCA/004/051/055/GCA_004051055.1_ASM405105v1/"
                    return Optional.of(currPath + dir.get().getName() + "/");
                }
            }
        }

        return Optional.empty();

    }

    /**
     * @param directoryPath The path of the directory in which target report is located relative to root of FTP server.
     *                      Eg:- "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/"
     * @return An InputStream of the first *assembly_report.txt file it finds.
     * @throws IOException Passes exception thrown by FTPBrowser.retrieveFileStream()
     */
    public InputStream getAssemblyReportInputStream(String directoryPath) throws IOException {

        InputStream fileStream;

        Stream<FTPFile> ftpFileStream = Arrays.stream(super.listFiles(directoryPath));
        Stream<FTPFile> assemblyReportFilteredStream = ftpFileStream.filter(
                f -> f.getName().contains("assembly_report.txt"));
        Optional<FTPFile> assemblyReport = assemblyReportFilteredStream.findFirst();

        if (assemblyReport.isPresent()) {
            directoryPath += assemblyReport.get().getName();
            fileStream = super.retrieveFileStream(directoryPath);
        } else {
            throw new IllegalArgumentException("Assembly Report File not present in given directory: " + directoryPath);
        }
        return fileStream;
    }

    public FTPFile getNCBIAssemblyReportFile(String directoryPath) throws IOException {
        Stream<FTPFile> ftpFileStream = Arrays.stream(super.listFiles(directoryPath));
        Stream<FTPFile> assemblyReportFilteredStream = ftpFileStream.filter(f -> f.getName().contains("assembly_report.txt"));
        Optional<FTPFile> assemblyReport = assemblyReportFilteredStream.findFirst();

        return assemblyReport.orElseThrow(() -> new AssemblyNotFoundException("Assembly Report File not present in given directory: " + directoryPath));
    }
}
