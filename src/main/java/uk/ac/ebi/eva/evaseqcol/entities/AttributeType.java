package uk.ac.ebi.eva.evaseqcol.entities;

import uk.ac.ebi.eva.evaseqcol.exception.AttributeNotDefinedException;

/**
 * The seqCol attributes stored as extended (level 2) data, with their name in the seqCol representation.
 * Only the inherent attributes contribute to the level 0 digest.
 * @see <a href="https://ga4gh.github.io/refget/seqcols/">GA4GH Sequence Collections specification</a>
 */
public enum AttributeType {
    names("names", true),
    sequences("sequences", true),
    md5DigestsOfSequences("md5_sequences", false),
    lengths("lengths", false),
    sortedNameLengthPairs("sorted_name_length_pairs", false);

    private final String attrVal;

    private final boolean inherent;

    AttributeType(String attrVal, boolean inherent) {
        this.attrVal = attrVal;
        this.inherent = inherent;
    }

    public String getAttrVal() {
        return attrVal;
    }

    public boolean isInherent() {
        return inherent;
    }

    /**
     * Return the enum type name given the attribute val
     */
    public static AttributeType fromAttributeVal(String attrVal) {
        for (AttributeType b : AttributeType.values()) {
            if (b.attrVal.equalsIgnoreCase(attrVal)) {
                return b;
            }
        }
        throw new AttributeNotDefinedException("No seqcol attribute with value \"" + attrVal + "\" found");
    }
}
