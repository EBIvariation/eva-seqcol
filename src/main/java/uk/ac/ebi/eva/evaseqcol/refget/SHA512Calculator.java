package uk.ac.ebi.eva.evaseqcol.refget;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class SHA512Calculator extends ChecksumCalculator{

    @Override
    /**
     * Calculate the refget checksum (ga4gh) of the given sequence
     * */
    public String calculateChecksum(String sequence) {
        String sequence1 = sequence.replaceAll("[\\W]|_", ""); // Remove non-alphanumeric characters
        byte[] digest =  SHA512Hash(sequence1.toUpperCase());

        // Encode the first 24 bytes with Base64
        byte[] truncatedSequence = Arrays.copyOfRange(digest, 0, 24);
        String encodedBase64Sequence = Base64.getUrlEncoder().encodeToString(truncatedSequence);
        return "SQ." + encodedBase64Sequence;
    }

    public byte[] SHA512Hash(String text) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(text.getBytes());
        return md.digest();
    }
}
