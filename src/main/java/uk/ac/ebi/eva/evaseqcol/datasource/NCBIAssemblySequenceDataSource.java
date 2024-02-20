package uk.ac.ebi.eva.evaseqcol.datasource;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReader;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIAssemblySequenceReaderFactory;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIBrowser;
import uk.ac.ebi.eva.evaseqcol.dus.NCBIBrowserFactory;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.utils.GzipCompress;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Repository("NCBISequenceDataSource")
public class NCBIAssemblySequenceDataSource implements AssemblySequencesDataSource{
    private final Logger logger = LoggerFactory.getLogger(NCBIAssemblySequenceDataSource.class);

    private final NCBIBrowserFactory factory;

    private final NCBIAssemblySequenceReaderFactory readerFactory;

    @Value("${asm.file.download.dir}")
    private String asmFileDownloadDir;

    @Autowired
    public NCBIAssemblySequenceDataSource(NCBIBrowserFactory factory,
                                          NCBIAssemblySequenceReaderFactory readerFactory){
        this.factory = factory;
        this.readerFactory = readerFactory;
    }

    public Optional<AssemblySequenceEntity> getAssemblySequencesByAccession(String insdcAccession, String fastFileContent) throws IOException {
        AssemblySequenceEntity assemblySequenceEntity;
        try (InputStream stream = new ByteArrayInputStream(fastFileContent.getBytes())) {
            NCBIAssemblySequenceReader reader = readerFactory.build(stream, insdcAccession);
            assemblySequenceEntity = reader.getAssemblySequencesEntity();
            logger.info("FASTA file content with accession " + insdcAccession + " has been parsed successfully");
        }

        return Optional.of(assemblySequenceEntity);
    }

    @Override
    public Optional<AssemblySequenceEntity> getAssemblySequencesByAccession(String accession) throws IOException, IllegalArgumentException {
        NCBIBrowser ncbiBrowser = factory.build();
        ncbiBrowser.connect();
        GzipCompress gzipCompress = new GzipCompress();

        Optional<Path> downloadFilePath = downloadAssemblySequences(accession, ncbiBrowser);
        if (!downloadFilePath.isPresent()) {
            return Optional.empty();
        }
        logger.info("Assembly FASTA " +  downloadFilePath.get().subpath(1,2) + " downloaded successfully in: " + downloadFilePath.get());
        // Uncompress the .gz file
        Optional<Path> compressedFilePath = gzipCompress.unzip(downloadFilePath.get().toString(), asmFileDownloadDir);
        if (!compressedFilePath.isPresent()){
            return Optional.empty();
        }

        AssemblySequenceEntity assemblySequenceEntity;
        try (InputStream stream = new FileInputStream(compressedFilePath.get().toFile())){
            NCBIAssemblySequenceReader reader = readerFactory.build(stream, accession);
            assemblySequenceEntity = reader.getAssemblySequencesEntity();
            logger.info("NCBI: Assembly FASTA with accession " + accession + " has been parsed successfully" );
        } finally {
            try {
                ncbiBrowser.disconnect();
                Files.deleteIfExists(downloadFilePath.get());
                Files.deleteIfExists(compressedFilePath.get()); // Deleting the fasta file
            } catch (IOException e) {
                //e.printStackTrace(); // We might want to uncomment this when debugging
                logger.warn("Error while trying to disconnect - ncbiBrowser (assembly: " + accession + ")");
            }
        }
        return Optional.of(assemblySequenceEntity);
    }


    /**
     * Download the assembly fna/fasta file given the accession and save it to /tmp
     * After this method is called, the file will be downloaded, and the path to this file
     * on your local computer will be returned*/
    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public Optional<Path> downloadAssemblySequences(String accession, NCBIBrowser ncbiBrowser) throws IOException {
        // The same directory as the report file
        Optional<String> directory = ncbiBrowser.getGenomeReportDirectory(accession);

        if (!directory.isPresent()) {
            return Optional.empty();
        }

        logger.info("NCBI directory for assembly FASTA download: " + directory.get());
        FTPFile ftpFile = ncbiBrowser.getAssemblySequencesFastaFile(directory.get());
        String ftpFilePath = directory.get() + ftpFile.getName();
        Path downloadFilePath = Paths.get(asmFileDownloadDir, ftpFile.getName());
        boolean success = ncbiBrowser.downloadFTPFile(ftpFilePath, downloadFilePath, ftpFile.getSize());
        if (success) {
            logger.info("NCBI assembly FASTA downloaded successfully (" + ftpFile.getName() + ")");
            return Optional.of(downloadFilePath);
        } else {
            logger.error("NCBI assembly FASTA could not be downloaded successfully(" + ftpFile.getName() + ")");
            return Optional.empty();
        }
    }
}
