package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONIntegerListExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONLevelOne;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONStringListExtData;
import uk.ac.ebi.eva.evaseqcol.exception.AttributeNotDefinedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Single place where the seqCol digests are computed:
 * <ul>
 *     <li>the digest of one attribute array (level 2 → level 1)</li>
 *     <li>the level 0 digest, computed from the inherent attributes of the level 1 object only</li>
 * </ul>
 * @see <a href="https://ga4gh.github.io/refget/seqcols/">GA4GH Sequence Collections specification, Encoding</a>
 */
@Service
public class SeqColDigestService {

    private final Logger logger = LoggerFactory.getLogger(SeqColDigestService.class);

    private final DigestCalculator digestCalculator;

    public SeqColDigestService(DigestCalculator digestCalculator) {
        this.digestCalculator = digestCalculator;
    }

    /**
     * Return the digest of one attribute's (level 2) array of values
     */
    public String calculateAttributeDigest(JSONExtData<?> attributeValues) throws IOException {
        return digestCalculator.getSha512Digest(attributeValues.toString());
    }

    /**
     * Return the level 0 digest of the given level 1 object
     */
    public String calculateLevelZeroDigest(JSONLevelOne levelOne) throws IOException {
        Map<String, String> levelOneMap = new TreeMap<>();
        for (AttributeType attributeType : AttributeType.values()) {
            levelOneMap.put(attributeType.getAttrVal(), levelOne.getDigest(attributeType));
        }
        return calculateSeqColLevelOneMapDigest(levelOneMap);
    }

    /**
     * Construct a seqCol level 1 (Map representation) out of the given seqColL2Map.
     * Attributes that are not defined are ignored.
     */
    public Map<String, String> constructSeqColLevelOneMap(Map<String, List<?>> seqColL2Map) throws IOException {
        Map<String, String> seqColL1Map = new TreeMap<>();
        for (Map.Entry<String, List<?>> attribute : seqColL2Map.entrySet()) {
            try {
                AttributeType attributeType = AttributeType.fromAttributeVal(attribute.getKey());
                seqColL1Map.put(attribute.getKey(),
                                calculateAttributeDigest(toExtData(attribute.getValue(), attributeType)));
            } catch (AttributeNotDefinedException e) {
                logger.warn(e.getMessage());
            }
        }
        return seqColL1Map;
    }

    /**
     * Return the level 0 digest of the given seqColLevelTwoMap, which is in the form of a Map (undefined attributes)
     */
    public String calculateSeqColLevelTwoMapDigest(Map<String, List<?>> seqColLevelTwoMap) throws IOException {
        return calculateSeqColLevelOneMapDigest(constructSeqColLevelOneMap(seqColLevelTwoMap));
    }

    /**
     * Return the level 0 digest of the given seqColLevelOneMap, which is in the form of a Map (undefined attributes).
     * Only the inherent attributes contribute to the digest.
     */
    public String calculateSeqColLevelOneMapDigest(Map<String, String> seqColLevelOneMap) throws IOException {
        return digestCalculator.getSha512Digest(toInherentAttributesJson(seqColLevelOneMap));
    }

    /**
     * Return the JSON representation of the inherent attributes of the given level 1 map,
     * e.g. {"names":"...","sequences":"..."}
     */
    private String toInherentAttributesJson(Map<String, String> seqColLevelOneMap) {
        StringBuilder json = new StringBuilder("{");
        for (AttributeType attributeType : AttributeType.values()) {
            String digest = seqColLevelOneMap.get(attributeType.getAttrVal());
            if (attributeType.isInherent() && digest != null) {
                if (json.length() > 1) {
                    json.append(",");
                }
                json.append("\"").append(attributeType.getAttrVal()).append("\":\"").append(digest).append("\"");
            }
        }
        return json.append("}").toString();
    }

    @SuppressWarnings("unchecked")
    private JSONExtData<?> toExtData(List<?> values, AttributeType attributeType) {
        if (attributeType == AttributeType.lengths) {
            return new JSONIntegerListExtData((List<Integer>) values);
        }
        return new JSONStringListExtData((List<String>) values);
    }
}
