package uk.ac.ebi.eva.evaseqcol.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONExtDataConverter;

/**
 * One extended (level 2) seqCol attribute (e.g. the array of names), identified by the digest of its content.
 * Instances are built by {@link uk.ac.ebi.eva.evaseqcol.service.SeqColExtendedDataFactory}.
 */
@Entity
@Data
@Table(name = "seqcol_extended_data")
public class SeqColExtendedDataEntity<T> {

    @Id
    @Column(name = "digest")
    protected String digest; // The digest of the attribute's content

    @Convert(converter = JSONExtDataConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JSONExtData<T> extendedSeqColData;

    @Transient
    private AttributeType attributeType;

    @Transient
    // This is needed when constructing multiple seqCol objects from the datasource to
    // identify the naming convention used for the sequences.
    // Note: This will probably be required by the namesAttributeList and might be null for the others
    private NamingConvention namingConvention;

    public SeqColExtendedDataEntity<T> setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
        return this;
    }

    public SeqColExtendedDataEntity<T> setExtendedSeqColData(JSONExtData<T> object) {
        this.extendedSeqColData = object;
        return this;
    }
}
