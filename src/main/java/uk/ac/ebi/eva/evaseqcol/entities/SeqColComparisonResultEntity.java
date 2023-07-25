package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SeqColComparisonResultEntity {
    /**
     * The "digests" attribute contains two sub-attributes:
     *  "a" and "b", which represents the digests of seqCol a and
     *  seqCol b respectively*/
    private Map<String, String> digests;

    /**
     * The "arrays" attribute contains three sub-attributes:
     *  "a-only", "b-only", "a-and-b"*/
    private Map<String, List<String>> arrays;

    /**
     * The "elements" attribute contains three sub-attributes:
     *  "total", "a-and-b", "a-and-b-same-order"*/
    private Map<String, Map<String, Object>> elements; // The object can be either Integer or Boolean


    public SeqColComparisonResultEntity() {
        this.digests = new HashMap<>();
        this.arrays = new HashMap<>();
        this.elements = new HashMap<>();
        elements.put("total", new HashMap<>());
        elements.put("a-and-b", new HashMap<>());
        elements.put("a-and-b-same-order", new HashMap<>());

    }

    /**
     * @param seqColId The seqCol identifier: "a" or "b"
     * @param digest The digest of the given seqCol*/
    public void putIntoDigests(String seqColId, String digest) {
        digests.put(seqColId, digest);
    }

    public void putIntoArrays(String key, List<String> value) {
        arrays.put(key, value);
    }

    public void putIntoElements(String elementName,String key, Object value) {
        Map<String, Object> elementsMap = elements.get(elementName);
        elementsMap.put(key, value);
    }
}
