package uk.ac.ebi.eva.evaseqcol.refget;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5ChecksumCalculator {

    /**
     * Return the MD5 checksum of the given sequence following the Refget API Specification v1.0.1
     * @param sequence the sequence of which we want to calculate the md5 checksum for
     * @return         the checksum of the given sequence
     * @see <a href="https://github.com/samtools/hts-specs/blob/master/refget.md">Refget API Specification v1.0.1</a>
     * */
    public String calculateChecksum(String sequence) {
        String sequence1 = sequence.replaceAll("[\\W]|_", ""); // Remove non-alphanumeric characters
        String sequence2 =  sequence1.toUpperCase();
        return md5Hash(sequence2);
    }

    /**
     * Return the digest of the text using the MD5 hashing algorithm*/
    public String md5Hash(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(text.getBytes());
        byte[] digest = md.digest();
        String textHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return textHash.toLowerCase();
    }
}
