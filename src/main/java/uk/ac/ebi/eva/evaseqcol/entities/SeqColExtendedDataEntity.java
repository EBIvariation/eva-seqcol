package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import uk.ac.ebi.eva.evaseqcol.refget.SHA512ChecksumCalculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Entity
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
@Data
@Table(name = "seqcol_extended_data")
public class SeqColExtendedDataEntity {

    @Id
    @Column(name = "digest")
    protected String digest; // The level 0 digest

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONExtData extendedSeqColData;

    @Transient
    private AttributeType attributeType;

    @Transient
    // This is needed when constructing multiple seqCol objects from the datasource to
    // identify the naming convention used for the sequences.
    private SeqColEntity.NamingConvention namingConvention;

    public enum AttributeType {
        names, sequences, md5DigestsOfSequences, lengths
    }

    public SeqColExtendedDataEntity setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
        return this;
    }

    public SeqColExtendedDataEntity setExtendedSeqColData(JSONExtData object) {
        this.extendedSeqColData = object;
        return this;
    }

    /**
     * Return the seqCol names array object*/
    public static SeqColExtendedDataEntity constructSeqColNamesObjectByNamingConvention(
            AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) {
        SeqColExtendedDataEntity seqColNamesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
        seqColNamesObject.setNamingConvention(convention);
        JSONExtData seqColNamesArray = new JSONExtData();
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
        SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setExtendedSeqColData(seqColNamesArray);
        seqColNamesObject.setDigest(sha512ChecksumCalculator.calculateChecksum(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    /**
     * Return the seqCol lengths array object*/
    public static SeqColExtendedDataEntity constructSeqColLengthsObject(AssemblyEntity assemblyEntity) {
        SeqColExtendedDataEntity seqColLengthsObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData seqColLengthsArray = new JSONExtData();
        List<String> lengthsList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            lengthsList.add(chromosome.getSeqLength().toString());
        }
        SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setExtendedSeqColData(seqColLengthsArray);
        seqColLengthsObject.setDigest(sha512ChecksumCalculator.calculateChecksum(seqColLengthsArray.toString()));
        return seqColLengthsObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity constructSeqColSequencesObject(AssemblySequenceEntity assemblySequenceEntity) {
        SeqColExtendedDataEntity seqColSequencesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData seqColSequencesArray = new JSONExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequence());
        }
        SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(sha512ChecksumCalculator.calculateChecksum(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity constructSeqColSequencesMd5Object(AssemblySequenceEntity assemblySequenceEntity) {
        SeqColExtendedDataEntity seqColSequencesObject = new SeqColExtendedDataEntity().setAttributeType(
                AttributeType.md5DigestsOfSequences);
        JSONExtData seqColSequencesArray = new JSONExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequenceMD5());
        }
        SHA512ChecksumCalculator sha512ChecksumCalculator = new SHA512ChecksumCalculator();
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(sha512ChecksumCalculator.calculateChecksum(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the list of extended data entities that are the same across multiple seqCol objects under
     * the same assembly accession. These entities are "sequences", "md5Sequences" and "lengths". */
    public static List<SeqColExtendedDataEntity> constructSameValueExtendedSeqColData(
            AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity) {
        return Arrays.asList(
                SeqColExtendedDataEntity.constructSeqColSequencesObject(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColSequencesMd5Object(assemblySequenceEntity),
                SeqColExtendedDataEntity.constructSeqColLengthsObject(assemblyEntity)
        );
    }

    /**
     * Return a list of seqCol sequences' names with all possible naming convention that can be extracted
     * from the given assemblyEntity*/
    public static List<SeqColExtendedDataEntity> constructAllPossibleExtendedNamesSeqColData(AssemblyEntity assemblyEntity) {
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
    }
}
