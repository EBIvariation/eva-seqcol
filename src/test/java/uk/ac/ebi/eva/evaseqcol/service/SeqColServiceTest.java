package uk.ac.ebi.eva.evaseqcol.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColComparisonResultEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.io.SeqColWriter;
import uk.ac.ebi.eva.evaseqcol.utils.SeqColMapConverter;


import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("seqcol")
//@Testcontainers
class SeqColServiceTest {


    private final String TEST_DIGEST = "eJ8GCVLEVtdnCN4OSqfkf6KoEOK9OUlr";

    private DigestCalculator digestCalculator = new DigestCalculator();

    @Autowired
    private SeqColService seqColService;

    @Autowired
    private SeqColWriter seqColWriter;


    /*@Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }*/

    @BeforeEach
    void setUp() throws IOException {
        seqColWriter.write(); // This will write some seqCol objects to the database
    }

    @AfterEach
    void tearDown() {
        //seqColWriter.clearData();
    }

    @Test
    void test() {

    }

    @Test
    @Transactional
    void getSeqColByDigestAndLevelTest() {
        Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestAndLevel(TEST_DIGEST, 1);
        assertTrue(levelOneEntity.isPresent());
        Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestAndLevel(TEST_DIGEST, 2);
        assertTrue(levelTwoEntity.isPresent());
    }

    @Test
    void unbalancedDuplicatesPresentTest() {
        List<String> A = Arrays.asList("1", "2", "2", "3", "4");
        List<String> B = Arrays.asList("1", "2", "3", "4", "5", "6");

        List<String> A1 = Arrays.asList("1", "2", "3", "4");
        List<String> B1 = Arrays.asList("1", "2", "3", "4", "5", "6");

        List<String> A2 = Arrays.asList("1", "2", "1");
        List<String> B2 = Arrays.asList("2", "1", "1");

        List<String> A3 = Arrays.asList("1", "2", "1");
        List<String> B3 = Arrays.asList("1", "2");

        assertTrue(seqColService.unbalancedDuplicatesPresent(A,B));
        assertFalse(seqColService.unbalancedDuplicatesPresent(A1, B1));
        assertFalse(seqColService.unbalancedDuplicatesPresent(A2, B2));
        assertTrue(seqColService.unbalancedDuplicatesPresent(A3, B3));
    }

    @Autowired
    private SeqColLevelTwoService levelTwoService;

    /**
     * Compare two seqCol objects; an already saved one: seqColA, with pre-defined attributes,
     * and undefined one: seqColB (unknown attributes). BE CAREFUL: the order of the arguments matters!!.
     * Note: of course the seqCol minimal required attributes should be present*/
    public SeqColComparisonResultEntity compareSeqCols(String seqColADigest, Map<String, List<String>> seqColBEntityMap) throws IOException {
        SeqColComparisonResultEntity comparisonResult = new SeqColComparisonResultEntity();
        Optional<SeqColLevelTwoEntity> seqColAEntity = levelTwoService.getSeqColLevelTwoByDigest(seqColADigest);

        String seqColBDigest = calculateSeqColLevelTwoMapDigest(seqColBEntityMap);

        // "digests" attribute
        comparisonResult.putIntoDigests("a", seqColADigest);
        comparisonResult.putIntoDigests("b", seqColBDigest);

        // Converting seqColA object into a Map in order to handle attributes generically (
        Map<String, List<String>> seqColAEntityMap = SeqColMapConverter.getSeqColLevelTwoMap(seqColAEntity.get());

        // Getting each seqCol object's attributes list
        Set<String> seqColAAttributeSet = seqColAEntityMap.keySet(); // The set of attributes in seqColAEntity
        Set<String> seqColBAttributeSet = seqColBEntityMap.keySet(); // The set of attributes in seqColBEntity
        List<String> seqColAAttributesList = new ArrayList<>(seqColAAttributeSet); // For better data manipulation
        List<String> seqColBAttributesList = new ArrayList<>(seqColBAttributeSet); // For better data manipulation

        // "arrays" attribute
        List<String> seqColAUniqueAttributes = getUniqueElements(seqColAAttributesList, seqColBAttributesList);
        List<String> seqColBUniqueAttributes = getUniqueElements(seqColBAttributesList, seqColAAttributesList);
        List<String> seqColCommonAttributes = getCommonElementsDistinct(seqColAAttributesList, seqColBAttributesList);
        comparisonResult.putIntoArrays("a-only", seqColAUniqueAttributes);
        comparisonResult.putIntoArrays("b-only", seqColBUniqueAttributes);
        comparisonResult.putIntoArrays("a-and-b", seqColCommonAttributes);

        // "elements" attribute | "total"
        String seqColARandomAttribute = seqColAAttributesList.get(0); // could be any attribute, normally they all have the same size
        String seqColBRandomAttribute = seqColBAttributesList.get(0); // could be any attribute, normally they all have the same size
        Integer seqColATotal = seqColAEntityMap.get(seqColARandomAttribute).size();
        Integer seqColBTotal = seqColBEntityMap.get(seqColBRandomAttribute).size();
        comparisonResult.putIntoElements("total", "a", seqColATotal);
        comparisonResult.putIntoElements("total", "b", seqColBTotal);

        // "elements" attribute | "a-and-b"
        List<String> commonSeqColAttributes = getCommonElementsDistinct(seqColAAttributesList, seqColBAttributesList);
        for (String element: commonSeqColAttributes) {
            Integer commonElementsCount = getCommonElementsCount(seqColAEntityMap.get(element), seqColBEntityMap.get(element));
            comparisonResult.putIntoElements("a-and-b", element, commonElementsCount);
        }

        // "elements" attribute | "a-and-b-same-order"
        for (String attribute: commonSeqColAttributes) {
            if (lessThanTwoOverlappingElements(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))
                    || unbalancedDuplicatesPresent(seqColAEntityMap.get(attribute), seqColBEntityMap.get(attribute))){
                comparisonResult.putIntoElements("a-and-b-same-order", attribute, null);
            } else {
                boolean attributeSameOrder = seqColAEntityMap.get(attribute).equals(seqColBEntityMap.get(attribute));
                comparisonResult.putIntoElements("a-and-b-same-order", "lengths", attributeSameOrder);
            }
        }

        return comparisonResult;
    }

    public boolean unbalancedDuplicatesPresent(List<String> listA, List<String> listB) {
        List<String> commonElements = getCommonElementsDistinct(listA, listB);
        Map<String, Map<String, Integer>> duplicatesCountMap = new HashMap<>();
        for (String element: commonElements) {
            Map<String, Integer> elementCount = new HashMap<>(); // Track the number of duplicates in each list for the same element
            elementCount.put("a", Collections.frequency(listA, element));
            elementCount.put("b", Collections.frequency(listB, element));
            duplicatesCountMap.put(element, elementCount);
        }
        for (Map<String, Integer> countMap: duplicatesCountMap.values()) {
            if (!Objects.equals(countMap.get("a"), countMap.get("b"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if there are less than two overlapping elements
     * @see 'https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#same-order-specification'*/
    public boolean lessThanTwoOverlappingElements(List<String> list1, List<String> list2) {
        //logger.info("less than two overlapping elements check: " + getCommonElementsDistinct(list1, list2).size());
        return getCommonElementsDistinct(list1, list2).size() < 2;
    }

    /**
     * Return the number of common elements between listA and listB
     * Note: Time complexity for this method is about O(n²)*/
    public Integer getCommonElementsCount(List<String> listA, List<String> listB) {
        List<String> listALocal = new ArrayList<>(listA); // we shouldn't be making changes on the actual lists
        List<String> listBLocal = new ArrayList<>(listB);
        int count = 0;
        // Looping over the smallest list will sometimes be time saver
        if (listALocal.size() < listBLocal.size()) {
            for (String element : listALocal) {
                if (listBLocal.contains(element)) {
                    count ++;
                    listBLocal.remove(element);
                }
            }
        } else {
            for (String element : listBLocal) {
                if (listALocal.contains(element)) {
                    count++;
                    listALocal.remove(element);
                }
            }
        }
        return count;
    }

    /**
     * Return fields of list1 that are not contained in list2*/
    public List<String> getUniqueElements(List<String> list1, List<String> list2) {
        List<String> tempList = new ArrayList<>(list1);
        tempList.removeAll(list2);
        return tempList;
    }

    /**
     * Return the list of the common elements between seqColAFields and seqColBFields (with no duplicates)*/
    public List<String> getCommonElementsDistinct(List<String> seqColAFields, List<String> seqColBFields) {
        List<String> commonFields = new ArrayList<>(seqColAFields);
        commonFields.retainAll(seqColBFields);
        List<String> commonFieldsDistinct = commonFields.stream().distinct().collect(Collectors.toList());
        return commonFieldsDistinct;
    }

    /**
     * Return the level 0 digest of the given seqColLevelTwoMap, which is in the form of a Map (undefined attributes)*/
    public String calculateSeqColLevelTwoMapDigest(Map<String, List<String>> seqColLevelTwoMap) throws IOException {
        Map<String, String> seqColLevelOne = constructSeqColLevelOneMap(seqColLevelTwoMap);
        String levelZeroDigest = calculateSeqColLevelOneMapDigest(seqColLevelOne);
        return levelZeroDigest;
    }



    /**
     * Return the level 0 digest of the given seqColLevelOneMap, which is in the form of a Map (undefined attributes)*/
    public String calculateSeqColLevelOneMapDigest(Map<String, String> seqColLevelOneMap) throws IOException {
        String seqColStandardRepresentation = convertSeqColLevelOneAttributeToString(seqColLevelOneMap);
        String levelZeroDigest = digestCalculator.getSha512Digest(seqColStandardRepresentation);
        return levelZeroDigest;
    }

    public Map<String, List<String>> generateSeqColLevel2Map() {
        Map<String, List<String>> seqColMap = new TreeMap<>();
        seqColMap.put("lengths", Arrays.asList("248956422", "242193529", "198295559"));
        seqColMap.put("names", Arrays.asList("A", "B", "C"));
        seqColMap.put("sequences", Arrays.asList(
                "CATAGAGCAGGTTTGAAACACTCTTTCTGTAGTATCTGCAAGCGGACGTTTCAAGCGCTTTCAGGCGT",
                "AAGTGGATATTTGGATAGCTTTGAGGATTTCGTTGGAAACGGGATTACATATAAAATCTAGAGAGAAGC",
                "GCTTGCAGATACTACAGAAAGAGTGTTTCAAACCTGCTCTATGAAAGGGAATGTTCAGTTCTGTGACTT"));
        return seqColMap;
    }

    public Map<String, String> generateSeqColLevelOneMap() {
        Map<String, String> seqColLevelOneMap = new TreeMap<>();
        seqColLevelOneMap.put("sequences", "EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe");
        seqColLevelOneMap.put("lengths", "5K4odB173rjao1Cnbk5BnvLt9V7aPAa2");
        seqColLevelOneMap.put("names", "g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp");
        return seqColLevelOneMap;
    }

    private boolean onlyDigits(String str) {
        String regex = "[0-9]+";
        Pattern p = Pattern.compile(regex);
        if (str == null) {
            return false;
        }
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * Return a normalized string representation of the given seqColL2Attribute
     * Note: This is the same method as the toString of the JSONExtData class*/
    private String convertSeqColLevelTwoAttributeToString(List<String> seqColL2Attribute) {
        StringBuilder objectStr = new StringBuilder();
        objectStr.append("[");
        if (onlyDigits(seqColL2Attribute.get(0).toString())) { // Lengths array, No quotes "...". Eg: [1111, 222, 333]
            for (int i=0; i<seqColL2Attribute.size()-1; i++) {
                objectStr.append(seqColL2Attribute.get(i));
                objectStr.append(",");
            }
            objectStr.append(seqColL2Attribute.get(seqColL2Attribute.size()-1));
            objectStr.append("]");
        } else { // Not a lengths array. Include quotes. Eg: ["aaa", "bbb", "ccc"].
            for (int i=0; i<seqColL2Attribute.size()-1; i++) {
                objectStr.append("\"");
                objectStr.append(seqColL2Attribute.get(i));
                objectStr.append("\"");
                objectStr.append(",");
            }
            objectStr.append("\"");
            objectStr.append(seqColL2Attribute.get(seqColL2Attribute.size()-1));
            objectStr.append("\"");
            objectStr.append("]");
        }
        return objectStr.toString();
    }

    /**
     * Return a normalized seqCol representation of the given seqColLevelOneMap
     * Note: This method is the same as the toString method of the SeqColLevelOneEntity class*/
    private String convertSeqColLevelOneAttributeToString(Map<String, String> seqColLevelOneMap) {
        StringBuilder seqColStringRepresentation = new StringBuilder();
        seqColStringRepresentation.append("{");
        for (String attribute: seqColLevelOneMap.keySet()) {
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(attribute);
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(":");
            seqColStringRepresentation.append("\"");
            seqColStringRepresentation.append(seqColLevelOneMap.get(attribute));
            seqColStringRepresentation.append("\","); // Pay attention for the last ","
        }
        // remove the last comma
        seqColStringRepresentation.replace(seqColStringRepresentation.length()-1, seqColStringRepresentation.length(), "");
        seqColStringRepresentation.append("}");
        return seqColStringRepresentation.toString();
    }

    /**
     * Construct a seqCol level 2 (Map representation) out of the given seqColL2Map*/
    public Map<String, String> constructSeqColLevelOneMap(Map<String, List<String>> seqColL2Map) throws IOException {
        Map<String, String> seqColL1Map = new TreeMap<>();
        Set<String> seqColAttributes = seqColL2Map.keySet(); // The set of the seqCol attributes ("lengths", "sequences", etc.)
        for (String attribute: seqColAttributes) {
            String attributeDigest = digestCalculator.getSha512Digest(
                    convertSeqColLevelTwoAttributeToString(seqColL2Map.get(attribute)));
            seqColL1Map.put(attribute, attributeDigest);
        }
        return seqColL1Map;
    }

    @Test
    void compareTest() throws IOException {
        Map<String, List<String>> seqColL2Map = generateSeqColLevel2Map() ;
        Map<String, String> seqColL1Map = constructSeqColLevelOneMap(seqColL2Map);
        assertEquals("5K4odB173rjao1Cnbk5BnvLt9V7aPAa2", seqColL1Map.get("lengths"));
        System.out.println(seqColL1Map);
        Map<String, String> seqColL1MapStatic = generateSeqColLevelOneMap();
        System.out.println("seqCol Level 1 String: " + seqColL1MapStatic.toString());
        assertEquals("S3LCyI788LE6vq89Tc_LojEcsMZRixzP", calculateSeqColLevelOneMapDigest(seqColL1MapStatic));
    }
}