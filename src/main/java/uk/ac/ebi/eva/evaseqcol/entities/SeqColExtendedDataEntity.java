package uk.ac.ebi.eva.evaseqcol.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import uk.ac.ebi.eva.evaseqcol.refget.SHA512Calculator;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.IOException;
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

    public enum AttributeType {
        names, sequences, sequencesMD5, lengths
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
    public static SeqColExtendedDataEntity constructSeqColNamesObject(AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
        SeqColExtendedDataEntity seqColNamesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.names);
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
        SHA512Calculator sha512Calculator = new SHA512Calculator();
        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setExtendedSeqColData(seqColNamesArray);
        seqColNamesObject.setDigest(sha512Calculator.calculateChecksum(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    /**
     * Return the seqCol lengths array object*/
    public static SeqColExtendedDataEntity constructSeqColLengthsObject(AssemblyEntity assemblyEntity) throws IOException {
        SeqColExtendedDataEntity seqColLengthsObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData seqColLengthsArray = new JSONExtData();
        List<String> lengthsList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            lengthsList.add(chromosome.getSeqLength().toString());
        }
        SHA512Calculator sha512Calculator = new SHA512Calculator();
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setExtendedSeqColData(seqColLengthsArray);
        seqColLengthsObject.setDigest(sha512Calculator.calculateChecksum(seqColLengthsArray.toString()));
        return seqColLengthsObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public static SeqColExtendedDataEntity constructSeqColSequencesObject(AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity seqColSequencesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData seqColSequencesArray = new JSONExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequenceMD5());
        }
        SHA512Calculator sha512Calculator = new SHA512Calculator();
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setExtendedSeqColData(seqColSequencesArray);
        seqColSequencesObject.setDigest(sha512Calculator.calculateChecksum(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }
}
