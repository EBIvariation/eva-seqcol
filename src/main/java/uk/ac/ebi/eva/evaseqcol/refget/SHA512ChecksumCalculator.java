package uk.ac.ebi.eva.evaseqcol.refget;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class SHA512ChecksumCalculator {

    /**
     * Calculate the GA4GH-SHA512 Refget checksum of the given sequence.
     * Adds the 'SQ.' at the beginning of the resulting digest.
     * Used to calculate the 'sequence's digest.
     * */
    public String calculateRefgetChecksum(String sequence) {
        return "SQ." + calculateChecksum(sequence);
    }

    /**
     * Calculate the GA4GH-SHA512 checksum of the given attribute.
     * Used to calculate the digest of attributes other than the 'sequence'.
     * */
    public String calculateChecksum(String input) {
        String sequence1 = input.replaceAll("[\\W]|_", ""); // Remove non-alphanumeric characters
        byte[] digest = SHA512Hash(sequence1.toUpperCase());

        // Encode the first 24 bytes with Base64
        byte[] truncatedSequence = Arrays.copyOfRange(digest, 0, 24);
        String encodedBase64Sequence = Base64.getUrlEncoder().encodeToString(truncatedSequence);
        return encodedBase64Sequence;
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


    public byte[] SHA512HashBytes(byte[] bytes) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(bytes);
        return md.digest();
    }
}
