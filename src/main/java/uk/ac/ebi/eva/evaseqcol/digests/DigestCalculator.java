package uk.ac.ebi.eva.evaseqcol.digests;

import org.erdtman.jcs.JsonCanonicalizer;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.evaseqcol.refget.SHA512ChecksumCalculator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Computes the GA4GH digest (RFC-8785 canonical JSON, SHA-512, truncated to 24 bytes, base64url) of a JSON string.
 * This class is stateless and therefore thread-safe.
 */
@Component
public class DigestCalculator {

    private final SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();

    /**
     * Return the GA4GH digest of the given JSON string representation.
     */
    public String getSha512Digest(String input) throws IOException {
        JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(input);
        byte[] hashed = sha512ChecksumCalculator.SHA512Hash(jsonCanonicalizer.getEncodedString());
        byte[] truncatedSequence = Arrays.copyOfRange(hashed, 0, 24);
        return Base64.getUrlEncoder().encodeToString(truncatedSequence);
    }
}
