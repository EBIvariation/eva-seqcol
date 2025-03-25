package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.exception.AttributeNotDefinedException;
import uk.ac.ebi.eva.evaseqcol.model.NameLengthPairEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONIntegerListExtData;
import uk.ac.ebi.eva.evaseqcol.utils.JSONStringListExtData;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Data
@Table(name = "seqcol_extended_data")
public class SeqColExtendedDataEntity<T> {

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONExtData<T> extendedSeqColData;

    @Transient
    private AttributeType attributeType;

    @Transient
    // This is needed when constructing multiple seqCol objects from the datasource to
    // identify the naming convention used for the sequences.
    // Note: This will probably be required by the namesAttributeList and might be null for the others
    private SeqColEntity.NamingConvention namingConvention;

    public enum AttributeType {
        names("names"),
        sequences("sequences"),
        md5DigestsOfSequences("md5_sequences"),
        lengths("lengths"),
        sortedNameLengthPairs("sorted_name_length_pairs");

        private String attrVal;

        AttributeType(String attrVal) {
            this.attrVal = attrVal;
        }

        public String getAttrVal() {
            return attrVal;
        }

        /**
         * Return the enum type name given the attribute val*/
        public static AttributeType fromAttributeVal(String attrVal) {
            for (AttributeType b : AttributeType.values()) {
                if (b.attrVal.equalsIgnoreCase(attrVal)) {
                    return b;
                }
            }
            throw new AttributeNotDefinedException("No seqcol attribute with value \"" + attrVal + "\" found");
        }
    }

    public SeqColExtendedDataEntity<T> setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
        return this;
    }

    public SeqColExtendedDataEntity<T> setExtendedSeqColData(JSONExtData<T> object) {
        this.extendedSeqColData = object;
        return this;
    }

    /**
     * Return the seqCol names array object*/
    public static SeqColExtendedDataEntity<List<String>> constructSeqColNamesObjectByNamingConvention(
            AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
        SeqColExtendedDataEntity<List<String>> seqColNamesObject = new SeqColExtendedDataEntity<List<String>>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
        seqColNamesObject.setNamingConvention(convention);
        JSONExtData<List<String>> seqColNamesArray = new JSONStringListExtData();
        List<String> namesList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            switch (convention) {
                case ENA:
                    namesList.add(chromosome.getEnaSequenceName());
                    break;
                case GENBANK:
                    namesList.add(chromosome.getGenbankSequenceName());
                    break;
                case UCSC:
                    namesList.add(chromosome.getUcscName());
                    break;
            }
        }
        DigestCalculator digestCalculator = new DigestCalculator();
        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setExtendedSeqColData(seqColNamesArray);
        seqColNamesObject.setDigest(digestCalculator.getSha512Digest(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    public static SeqColExtendedDataEntity<List<String>> constructSeqColNamesObjectWithRefSeqAndTESTNamingConvention(
            AssemblySequenceEntity sequenceEntity) throws IOException {
        SeqColExtendedDataEntity<List<String>> seqColNamesObject = new SeqColExtendedDataEntity<List<String>>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
        seqColNamesObject.setNamingConvention(SeqColEntity.NamingConvention.TEST);
        JSONExtData<List<String>> seqColNamesArray = new JSONStringListExtData();
        List<String> namesList = sequenceEntity.getSequences().stream().map(s -> s.getRefseq()).collect(Collectors.toList());
        DigestCalculator digestCalculator = new DigestCalculator();
        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setExtendedSeqColData(seqColNamesArray);
        seqColNamesObject.setDigest(digestCalculator.getSha512Digest(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    /**
     * Return the seqCol lengths array object*/
    public static SeqColExtendedDataEntity<List<Integer>> constructSeqColLengthsObject(AssemblyEntity assemblyEntity) throws IOException {
        SeqColExtendedDataEntity<List<Integer>> seqColLengthsObject = new SeqColExtendedDataEntity<List<Integer>>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData<List<Integer>> seqColLengthsArray = new JSONIntegerListExtData();
        List<Integer> lengthsList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            lengthsList.add(Math.toIntExact(chromosome.getSeqLength()));
        }
        DigestCalculator digestCalculator = new DigestCalculator();
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setExtendedSeqColData(seqColLengthsArray);
        seqColLengthsObject.setDigest(digestCalculator.getSha512Digest(seqColLengthsArray.toString()));

        return seqColLengthsObject;
    }


    public static SeqColExtendedDataEntity<List<Integer>> constructSeqColLengthsObject(AssemblySequenceEntity sequenceEntity) throws IOException {
        SeqColExtendedDataEntity<List<Integer>> seqColLengthsObject = new SeqColExtendedDataEntity<List<Integer>>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData<List<Integer>> seqColLengthsArray = new JSONIntegerListExtData();
        List<Integer> lengthsList = sequenceEntity.getSequences().stream().map(s -> s.getLength()).collect(Collectors.toList());

        DigestCalculator digestCalculator = new DigestCalculator();
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setExtendedSeqColData(seqColLengthsArray);
        seqColLengthsObject.setDigest(digestCalculator.getSha512Digest(seqColLengthsArray.toString()));

        return seqColLengthsObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity<List<String>> constructSeqColSequencesObject(
            AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity<List<String>> seqColSequencesObject = new SeqColExtendedDataEntity<List<String>>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData<List<String>> seqColSequencesArray = new JSONStringListExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequence());
        }
        DigestCalculator digestCalculator = new DigestCalculator();
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(digestCalculator.getSha512Digest(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity<List<String>> constructSeqColSequencesMd5Object(
            AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity<List<String>> seqColSequencesObject = new SeqColExtendedDataEntity<List<String>>().setAttributeType(
                AttributeType.md5DigestsOfSequences);
        JSONExtData<List<String>> seqColSequencesArray = new JSONStringListExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequenceMD5());
        }
        DigestCalculator digestCalculator = new DigestCalculator();
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(digestCalculator.getSha512Digest(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the seqCol sorted-name-length-pairs extended object*/
    public static SeqColExtendedDataEntity<List<String>> constructSeqColSortedNameLengthPairs(
            SeqColExtendedDataEntity<List<String>> extendedNames, SeqColExtendedDataEntity<List<Integer>> extendedLengths) throws IOException {
        if (extendedNames.getExtendedSeqColData().getObject().size() != extendedLengths.getExtendedSeqColData().getObject().size()) {
            return null; // Names and Lengths entities are not compatible
        }
        SeqColExtendedDataEntity<List<String>> SeqColSortedNameLengthPairsObject = new SeqColExtendedDataEntity<List<String>>().setAttributeType(
                AttributeType.sortedNameLengthPairs);
        JSONExtData<List<String>> seqColSortedNameLengthPairsArray = new JSONStringListExtData();

        // Get the plain name-length pairs
        List<NameLengthPairEntity> nameLengthPairList = constructNameLengthPairList(extendedNames, extendedLengths);
        // Get the sorted list
        List<String> sortedNameLengthPairsList = constructSortedNameLengthPairs(nameLengthPairList);

        DigestCalculator digestCalculator = new DigestCalculator();
        seqColSortedNameLengthPairsArray.setObject(sortedNameLengthPairsList);
        SeqColSortedNameLengthPairsObject.setExtendedSeqColData(seqColSortedNameLengthPairsArray);
        SeqColSortedNameLengthPairsObject.setDigest(digestCalculator.getSha512Digest(
                seqColSortedNameLengthPairsArray.toString()));
        return SeqColSortedNameLengthPairsObject;
    }

    /**
     * Retrieve and construct the list of name-length pairs*/
    private static List<NameLengthPairEntity> constructNameLengthPairList(
            SeqColExtendedDataEntity<List<String>> extendedNames, SeqColExtendedDataEntity<List<Integer>> extendedLengths) {
        List<NameLengthPairEntity> nameLengthPairList = new ArrayList<>();
        for (int i=0; i<extendedNames.getExtendedSeqColData().getObject().size(); i++) {
            String name = extendedNames.getExtendedSeqColData().getObject().get(i);
            Integer length = extendedLengths.getExtendedSeqColData().getObject().get(i);
            nameLengthPairList.add(new NameLengthPairEntity(name, length));
        }
        return nameLengthPairList;
    }

    /**
     * Return the sorted-name-length-pair list for the given list of nameLengthPairEntity*/
    public static List<String> constructSortedNameLengthPairs(List<NameLengthPairEntity> nameLengthPairList) throws IOException {
        DigestCalculator digestCalculator = new DigestCalculator();
        List<String> sortedNameLengthPairs = new ArrayList<>();
        for (NameLengthPairEntity entity: nameLengthPairList) {
            String nameLengthHash = digestCalculator.getSha512Digest(entity.toString());
            sortedNameLengthPairs.add(nameLengthHash);
        }
        // Sorting the name-length-pair list according to the elements' natural order (alphanumerically)
        Comparator<String> nameLengthComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        };
        sortedNameLengthPairs.sort(nameLengthComparator);
        return sortedNameLengthPairs;
    }

    /**
     * Return a list of seqCol sequences' names with all possible naming convention that can be extracted
     * from the given assemblyEntity*/
    public static List<SeqColExtendedDataEntity<List<String>>> constructAllPossibleExtendedNamesSeqColData(
            AssemblyEntity assemblyEntity) throws IOException {
        List<SeqColEntity.NamingConvention> existingNamingConventions = new ArrayList<>();
        if (assemblyEntity.getChromosomes().get(0).getEnaSequenceName() != null) {
            existingNamingConventions.add(SeqColEntity.NamingConvention.ENA);
        }
        if (assemblyEntity.getChromosomes().get(0).getInsdcAccession() != null) {
            existingNamingConventions.add(SeqColEntity.NamingConvention.GENBANK);
        }
        if (assemblyEntity.getChromosomes().get(0).getUcscName() != null) {
            existingNamingConventions.add(SeqColEntity.NamingConvention.UCSC);
        }

        List<SeqColExtendedDataEntity<List<String>>> allPossibleExtendedNamesData = new ArrayList<>();
        for (SeqColEntity.NamingConvention convention: existingNamingConventions) {
            SeqColExtendedDataEntity<List<String>> extendedNamesEntity = constructSeqColNamesObjectByNamingConvention(assemblyEntity, convention);
            extendedNamesEntity.setNamingConvention(convention);
            allPossibleExtendedNamesData.add(extendedNamesEntity);
        }
        return allPossibleExtendedNamesData;
    }
}
