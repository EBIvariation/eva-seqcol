package uk.ac.ebi.eva.evaseqcol.dus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class ENABrowser {

    public static final String EBI_SERVER = "https://ftp.ebi.ac.uk";

    public static final String PATH_ENA_ASSEMBLY = "/pub/databases/ena/assembly/";

    private final HttpFileBrowser browser;

    public ENABrowser() {
        this.browser = new HttpFileBrowser();
    }

    /**
     * Takes INSDC accession and gets the corresponding assembly report.
     * For example, on input "GCA_003005035.1" it will return a stream to the file at
     * ftp.ebi.ac.uk/pub/databases/ena/assembly/GCA_003/GCA_003005/GCA_003005035.1_sequence_report.txt
     *
     * @param accession Any GCA accession
     * @return Input stream of the corresponding sequence_report.txt file.
     * @throws IOException Passes exception thrown while fetching the remote file
     */
    public InputStream getAssemblyReportInputStream(String accession) throws IOException, IllegalArgumentException {
        String fullPath = getAssemblyDirPath(accession) + accession + "_sequence_report.txt";
        return browser.openStream(EBI_SERVER + fullPath);
    }

    public String getAssemblyDirPath(String accession) {
        if (accession.length() < 15) {
            throw new IllegalArgumentException("Accession should be at least 15 characters long!");
        }
        String directory = accession.substring(0, 7) + "/" + accession.substring(0, 10) + "/";
        return PATH_ENA_ASSEMBLY + directory;
    }

    public RemoteFile getAssemblyReportFile(String dirPath, String accession) throws IOException {
        String expectedName = accession + "_sequence_report.txt";
        List<String> entries = browser.listDirectory(EBI_SERVER + dirPath);
        String name = entries.stream()
                              .filter(entry -> entry.equals(expectedName))
                              .findFirst()
                              .orElseThrow(() -> new IllegalArgumentException(
                                      "Assembly Report File not present in given directory: " + dirPath));
        long size = browser.headContentLength(EBI_SERVER + dirPath + name);
        return new RemoteFile(name, size);
    }

    public boolean downloadFile(String filePath, Path downloadFilePath, long expectedSize) throws IOException {
        return browser.downloadFile(EBI_SERVER + filePath, downloadFilePath, expectedSize);
    }

}
