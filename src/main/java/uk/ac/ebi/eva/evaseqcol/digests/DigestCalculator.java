package uk.ac.ebi.eva.evaseqcol.digests;

import org.erdtman.jcs.JsonCanonicalizer;

import uk.ac.ebi.eva.evaseqcol.refget.SHA512ChecksumCalculator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class DigestCalculator {
    private SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
    private JsonCanonicalizer jc;

    /**
     * Generate the level 0 digest given a string representation of a seqCol
     * */
    public String getSha512Digest(String input) throws IOException {
        jc = new JsonCanonicalizer(input);
        byte[] hashed =  sha512ChecksumCalculator.SHA512Hash(jc.getEncodedString());
        byte[] truncatedSequence = Arrays.copyOfRange(hashed, 0, 24);
        String text = Base64.getUrlEncoder().encodeToString(truncatedSequence);
        return text;
    }
}
