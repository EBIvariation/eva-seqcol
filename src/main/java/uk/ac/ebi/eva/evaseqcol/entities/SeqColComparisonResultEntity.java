package uk.ac.ebi.eva.evaseqcol.entities;

import lombok.Data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class SeqColComparisonResultEntity {
    /**
     * The "digests" attribute contains two sub-attributes:
     *  "a" and "b", which represents the digests of seqCol a and
     *  seqCol b respectively*/
    private SortedMap<String, String> digests;

    /**
     * The "attributes" attribute contains three sub-attributes:
     *  "a_only", "b_only", "a_and_b"*/
    private SortedMap<String, List<String>> attributes;

    /**
     * The "array_elements" attribute contains four sub-attributes:
     *  "a_count", "b_count", "a_and_b_count", "a_and_b_same_order"*/
    private HashMap<String, TreeMap<String, Object>> array_elements; // The object can be either Integer or Boolean


    public SeqColComparisonResultEntity() {
        this.digests = new TreeMap<>();
        this.attributes = new TreeMap<>();
        this.array_elements = new LinkedHashMap<>();
        array_elements.put("a_count", new TreeMap<>());
        array_elements.put("b_count", new TreeMap<>());
        array_elements.put("a_and_b_count", new TreeMap<>());
        array_elements.put("a_and_b_same_order", new TreeMap<>());

    }

    /**
     * @param seqColId The seqCol identifier: "a" or "b"
     * @param digest The digest of the given seqCol*/
    public void putIntoDigests(String seqColId, String digest) {
        digests.put(seqColId, digest);
    }

    public void putIntoArrays(String key, List<String> value) {
        attributes.put(key, value);
    }

    public void putIntoArrayElements(String elementName, String key, Object value) {
        SortedMap<String, Object> arrayElementsMap = array_elements.get(elementName);
        arrayElementsMap.put(key, value);
    }
}
