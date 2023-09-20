package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;

@Data
public class SeqColComparisonResultEntity {
    /**
     * The "digests" attribute contains two sub-attributes:
     *  "a" and "b", which represents the digests of seqCol a and
     *  seqCol b respectively*/
    private SortedMap<String, String> digests;

    /**
     * The "arrays" attribute contains three sub-attributes:
     *  "a_only", "b_only", "a_and_b"*/
    private SortedMap<String, List<String>> arrays;

    /**
     * The "elements" attribute contains three sub-attributes:
     *  "total", "a_and_b", "a_and_b_same_order"*/
    private SortedMap<String, SortedMap<String, Object>> elements; // The object can be either Integer or Boolean


    public SeqColComparisonResultEntity() {
        this.digests = new TreeMap<>();
        this.arrays = new TreeMap<>();
        this.elements = new TreeMap<>();
        elements.put("total", new TreeMap<>());
        elements.put("a_and_b", new TreeMap<>());
        elements.put("a_and_b_same_order", new TreeMap<>());

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
        SortedMap<String, Object> elementsMap = elements.get(elementName);
        elementsMap.put(key, value);
    }
}
