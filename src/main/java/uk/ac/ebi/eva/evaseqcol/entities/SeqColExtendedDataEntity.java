package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.model.NameLengthPairEntity;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
    private SeqColEntity.NamingConvention namingConvention;

    public enum AttributeType {
        names, sequences, md5DigestsOfSequences, lengths, sortedNameLengthPairs
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
    public static SeqColExtendedDataEntity<String> constructSeqColNamesObjectByNamingConvention(
            AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
        SeqColExtendedDataEntity<String> seqColNamesObject = new SeqColExtendedDataEntity<String>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
        seqColNamesObject.setNamingConvention(convention);
        JSONExtData<String> seqColNamesArray = new JSONExtData<>();
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

    /**
     * Return the seqCol lengths array object*/
    public static SeqColExtendedDataEntity<Integer> constructSeqColLengthsObject(AssemblyEntity assemblyEntity) throws IOException {
        SeqColExtendedDataEntity<Integer> seqColLengthsObject = new SeqColExtendedDataEntity<Integer>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData<Integer> seqColLengthsArray = new JSONExtData<>();
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

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity<String> constructSeqColSequencesObject(
            AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity<String> seqColSequencesObject = new SeqColExtendedDataEntity<String>().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData<String> seqColSequencesArray = new JSONExtData<>();
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
    public static SeqColExtendedDataEntity<String> constructSeqColSequencesMd5Object(
            AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity<String> seqColSequencesObject = new SeqColExtendedDataEntity<String>().setAttributeType(
                AttributeType.md5DigestsOfSequences);
        JSONExtData<String> seqColSequencesArray = new JSONExtData<>();
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
    public static SeqColExtendedDataEntity<String> constructSeqColSortedNameLengthPairs(
            SeqColExtendedDataEntity<String> extendedNames, SeqColExtendedDataEntity<Integer> extendedLengths) throws IOException {
        if (extendedNames.getExtendedSeqColData().getObject().size() != extendedLengths.getExtendedSeqColData().getObject().size()) {
            return null; // Names and Lengths entities are not compatible
        }
        SeqColExtendedDataEntity<String> SeqColSortedNameLengthPairsObject = new SeqColExtendedDataEntity<String>().setAttributeType(
                AttributeType.sortedNameLengthPairs);
        JSONExtData<String> seqColSortedNameLengthPairsArray = new JSONExtData<>();

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
            SeqColExtendedDataEntity<String> extendedNames, SeqColExtendedDataEntity<Integer> extendedLengths) {
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
     * Return the list of extended data entities that are the same across multiple seqCol objects under
     * the same assembly accession. These entities are "sequences", "md5Sequences" and "lengths". */
    //TODO: CHANGE LOGIC
    /*public static List<SeqColExtendedDataEntity> constructSameValueExtendedSeqColData(
            AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        return Arrays.asList(
                SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity)
        );
    }*/

    /**
     * Return a list of seqCol sequences' names with all possible naming convention that can be extracted
     * from the given assemblyEntity*/
    //TODO: CHANGE LOGIC
    /*public static List<SeqColExtendedDataEntity> constructAllPossibleExtendedNamesSeqColData(
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

        List<SeqColExtendedDataEntity> allPossibleExtendedNamesData = new ArrayList<>();
        for (SeqColEntity.NamingConvention convention: existingNamingConventions) {
            SeqColExtendedDataEntity extendedNamesEntity = constructSeqColNamesObjectByNamingConvention(assemblyEntity, convention);
            extendedNamesEntity.setNamingConvention(convention);
            allPossibleExtendedNamesData.add(extendedNamesEntity);
        }
        return allPossibleExtendedNamesData;
    }*/
}
