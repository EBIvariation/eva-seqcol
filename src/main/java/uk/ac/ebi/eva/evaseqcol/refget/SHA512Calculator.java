package uk.ac.ebi.eva.evaseqcol.refget;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512Calculator extends ChecksumCalculator{

    @Override
    public String calculateChecksum(String sequence) {
        String sequence1 = sequence.replaceAll("[\\W]|_", ""); // Remove non-alphanumeric characters
        String sequence2 =  sequence1.toUpperCase();
        return SHA512Hash(sequence2);
    }

    public String SHA512Hash(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
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
