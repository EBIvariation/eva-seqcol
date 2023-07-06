package uk.ac.ebi.eva.evaseqcol.refget;

import org.erdtman.jcs.JsonCanonicalizer;
import org.hibernate.event.spi.SaveOrUpdateEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

public class DigestCalculator {
    private SHA512Calculator sha512Calculator = new SHA512Calculator();
    private JsonCanonicalizer jc;

    /**
     * Generate the level0 digest out of a level 1 object.
     * LevelOneObject: A json object that has three elements: Sequences, names and lengths.
     * */
    public String getDigest(String input) throws IOException {
        jc = new JsonCanonicalizer(input);
        byte[] hashed =  sha512Calculator.SHA512Hash(jc.getEncodedString());
        byte[] truncatedSequence = Arrays.copyOfRange(hashed, 0, 24);
        String text = Base64.getUrlEncoder().encodeToString(truncatedSequence);
        return text;
    }
}
