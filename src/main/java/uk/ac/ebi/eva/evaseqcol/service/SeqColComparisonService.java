package uk.ac.ebi.eva.evaseqcol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColComparisonResultEntity;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.utils.SeqColMapConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Comparison of two seqCol objects.
 * @see <a href="https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#2022-06-15---structure-for-the-return-value-of-the-comparison-api-endpoint">
 *     Structure of the comparison result</a>
 */
@Service
public class SeqColComparisonService {

    private final Logger logger = LoggerFactory.getLogger(SeqColComparisonService.class);

    private final SeqColLevelTwoService levelTwoService;

    private final SeqColDigestService digestService;

    public SeqColComparisonService(SeqColLevelTwoService levelTwoService, SeqColDigestService digestService) {
        this.levelTwoService = levelTwoService;
        this.digestService = digestService;
    }

    /**
     * Compare two seqCols given their level 0 digests (Used to compare two local seqCol objects)
     * @throws SeqColNotFoundException if one of them does not exist
     */
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, String seqColBDigest) {
        SeqColLevelTwoEntity seqColAEntity = getLevelTwo(seqColADigest);
        SeqColLevelTwoEntity seqColBEntity = getLevelTwo(seqColBDigest);
        return compareSeqCols(seqColADigest, SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity),
                              seqColBDigest, SeqColMapConverter.getSeqColLevelTwoMap(seqColBEntity));
    }

    /**
     * Compare two seqCol objects; an already saved one: seqColA, with pre-defined attributes,
     * and undefined one: seqColB (unknown attributes). BE CAREFUL: the order of the arguments matters!!.
     * Note: of course the seqCol minimal required attributes should be present
     * @throws SeqColNotFoundException if seqColA does not exist
     */
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, Map<String, List<?>> seqColBEntityMap)
            throws IOException {
        SeqColLevelTwoEntity seqColAEntity = getLevelTwo(seqColADigest);
        String seqColBDigest = digestService.calculateSeqColLevelTwoMapDigest(seqColBEntityMap);
        return compareSeqCols(seqColADigest, SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity),
                              seqColBDigest, seqColBEntityMap);
    }

    /**
     * Compare two seqCol L2 objects
     */
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, Map<String, List<?>> seqColAEntityMap,
                                                       String seqColBDigest, Map<String, List<?>> seqColBEntityMap) {
        logger.info("Comparing seqCol {} and seqCol {}", seqColADigest, seqColBDigest);
        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();

        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);

        // "attributes" attribute
        List<String> seqColAAttributes = new ArrayList<>(seqColAEntityMap.keySet());
        List<String> seqColBAttributes = new ArrayList<>(seqColBEntityMap.keySet());
        List<String> commonAttributes = getCommonElementsDistinct(seqColAAttributes, seqColBAttributes);
        comparisonResult.putIntoArrays("a_only", getUniqueElements(seqColAAttributes, seqColBAttributes));
        comparisonResult.putIntoArrays("b_only", getUniqueElements(seqColBAttributes, seqColAAttributes));
        comparisonResult.putIntoArrays("a_and_b", commonAttributes);

        // "array_elements" attribute | "a_count" and "b_count"
        for (String attribute : seqColAAttributes) {
            comparisonResult.putIntoArrayElements("a_count", attribute, seqColAEntityMap.get(attribute).size());
        }
        for (String attribute : seqColBAttributes) {
            comparisonResult.putIntoArrayElements("b_count", attribute, seqColBEntityMap.get(attribute).size());
        }

        // "array_elements" attribute | "a_and_b_count" and "a_and_b_same_order"
        for (String attribute : commonAttributes) {
            List<?> valuesA = seqColAEntityMap.get(attribute);
            List<?> valuesB = seqColBEntityMap.get(attribute);
            comparisonResult.putIntoArrayElements("a_and_b_count", attribute, getCommonElementsCount(valuesA, valuesB));
            Boolean sameOrder = null; // Order is undefined in these cases
            if (!lessThanTwoOverlappingElements(valuesA, valuesB) && !unbalancedDuplicatesPresent(valuesA, valuesB)) {
                sameOrder = isSameOrder(valuesA, valuesB);
            }
            comparisonResult.putIntoArrayElements("a_and_b_same_order", attribute, sameOrder);
        }
        return comparisonResult;
    }

    private SeqColLevelTwoEntity getLevelTwo(String digest) {
        return levelTwoService.getSeqColLevelTwoByDigest(digest).orElseThrow(() -> {
            logger.warn("No seqCol corresponding to digest {} could be found in the db", digest);
            return new SeqColNotFoundException(digest);
        });
    }

    /**
     * Check whether the array of elements of elementsA are in the same order as the ones in elementsB.
     * Example 1:
     *      A = ["ch1", "B", "ch2", "ch3"],
     *      B = ["ch1", "A", "ch2", "ch3"],
     *      Common = ["ch1", "ch2", "ch3"] # Common elements between A and B
     *      ==> A: indexOf("ch1") < indexOf("ch2") < indexOf("ch3") and B: indexOf("ch1") < indexOf("ch2") < indexOf("ch3")
     *      ==> Same order elements
     * Example 2:
     *      A = ["ch1", "B", "ch2", "ch3"]
     *      B = ["A", "ch1", "ch2", "ch3"]
     *      Common = ["ch1", "ch2", "ch3"]
     *      ==> A: indexOf("ch1") < indexOf("ch2") < indexOf("ch3") and B: indexOf("ch1") < indexOf("ch2") < indexOf("ch3")
     *      ==> Same order elements
     * NOTE: List.retainAll() preserves the order of the original list
     * @see "https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification"
     */
    boolean isSameOrder(List<?> elementsA, List<?> elementsB) {
        LinkedList<?> elementsALocal = new LinkedList<>(elementsA);
        LinkedList<?> elementsBLocal = new LinkedList<>(elementsB);
        List<?> commonElements = getCommonElementsDistinct(elementsALocal, elementsBLocal);
        elementsALocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)
        elementsBLocal.retainAll(commonElements); // Leaving only the common elements (keeping the original order to check)
        return elementsALocal.equals(elementsBLocal);
    }

    /**
     * Return fields of list1 that are not contained in list2
     */
    List<String> getUniqueElements(List<String> list1, List<String> list2) {
        List<String> tempList = new ArrayList<>(list1);
        tempList.removeAll(list2);
        return tempList;
    }

    /**
     * Return the list of the common elements between listA and listB (with no duplicates, in the order of listA)
     */
    @SuppressWarnings("unchecked")
    <T> List<T> getCommonElementsDistinct(List<? extends T> listA, List<?> listB) {
        List<T> commonFields = new ArrayList<>(listA);
        commonFields.retainAll(listB);
        return commonFields.stream().distinct().toList();
    }

    /**
     * Return the number of distinct common elements between listA and listB
     */
    int getCommonElementsCount(List<?> listA, List<?> listB) {
        Set<?> listALocal = new HashSet<>(listA); // we shouldn't be making changes on the actual lists
        listALocal.retainAll(new HashSet<>(listB));
        return listALocal.size();
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'
     */
    boolean lessThanTwoOverlappingElements(List<?> list1, List<?> list2) {
        return getCommonElementsDistinct(list1, list2).size() < 2;
    }

    /**
     * Return true if there are unbalanced duplicates present, i.e. a common element that is not repeated the same
     * number of times in both lists.
     * Example 1: A = [1, 2, 3, 4]
     *            B = [1, 2, 3, 4, 5, 6]
     *            No duplicates (balanced duplicates)
     *
     * Example 2: A = [1, 2, 2, 3, 4]
     *            B = [1, 2 ,3, 4]
     *            2: {A:2, B:1} -> Unbalanced duplicates
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'
     */
    boolean unbalancedDuplicatesPresent(List<?> listA, List<?> listB) {
        for (Object element : getCommonElementsDistinct(listA, listB)) {
            if (Collections.frequency(listA, element) != Collections.frequency(listB, element)) {
                return true;
            }
        }
        return false;
    }
}
