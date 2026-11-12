package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.evaseqcol.digests.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.dto.NameLengthPairEntity;
import uk.ac.ebi.eva.evaseqcol.dto.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.NamingConvention;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataSet;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONIntegerListExtData;
import uk.ac.ebi.eva.evaseqcol.entities.json.JSONStringListExtData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the extended (level 2) seqCol attributes, with their digests, from the assembly report / FASTA data
 * or from an existing level 2 object.
 */
@Component
public class SeqColExtendedDataFactory {

    private final SeqColDigestService digestService;

    private final DigestCalculator digestCalculator;

    public SeqColExtendedDataFactory(SeqColDigestService digestService, DigestCalculator digestCalculator) {
        this.digestService = digestService;
        this.digestCalculator = digestCalculator;
    }

    /**
     * Return the seqCol names array object for the given naming convention
     */
    public SeqColExtendedDataEntity<List<String>> namesByNamingConvention(
            AssemblyEntity assemblyEntity, NamingConvention convention) throws IOException {
        List<String> namesList = new ArrayList<>();
        for (SequenceEntity chromosome : assemblyEntity.getChromosomes()) {
            switch (convention) {
                case ENA -> namesList.add(chromosome.getEnaSequenceName());
                case GENBANK -> namesList.add(chromosome.getGenbankSequenceName());
                case UCSC -> namesList.add(chromosome.getUcscName());
                default -> throw new IllegalArgumentException(
                        "Naming convention " + convention + " is not available in assembly reports");
            }
        }
        return names(namesList, convention);
    }

    /**
     * Return the seqCol names array object using the FASTA headers (refseq) as names and the TEST naming convention
     */
    public SeqColExtendedDataEntity<List<String>> namesFromFastaHeaders(AssemblySequenceEntity sequenceEntity)
            throws IOException {
        List<String> namesList = sequenceEntity.getSequences().stream().map(SeqColSequenceEntity::getRefseq).toList();
        return names(namesList, NamingConvention.TEST);
    }

    /**
     * Return a list of seqCol sequences' names with all possible naming convention that can be extracted
     * from the given assemblyEntity
     */
    public List<SeqColExtendedDataEntity<List<String>>> allPossibleNames(AssemblyEntity assemblyEntity)
            throws IOException {
        ChromosomeEntity firstChromosome = assemblyEntity.getChromosomes().get(0);
        List<NamingConvention> existingNamingConventions = new ArrayList<>();
        if (firstChromosome.getEnaSequenceName() != null) {
            existingNamingConventions.add(NamingConvention.ENA);
        }
        if (firstChromosome.getInsdcAccession() != null) {
            existingNamingConventions.add(NamingConvention.GENBANK);
        }
        if (firstChromosome.getUcscName() != null) {
            existingNamingConventions.add(NamingConvention.UCSC);
        }

        List<SeqColExtendedDataEntity<List<String>>> allPossibleExtendedNamesData = new ArrayList<>();
        for (NamingConvention convention : existingNamingConventions) {
            allPossibleExtendedNamesData.add(namesByNamingConvention(assemblyEntity, convention));
        }
        return allPossibleExtendedNamesData;
    }

    /**
     * Return the seqCol lengths array object from the assembly report
     */
    public SeqColExtendedDataEntity<List<Integer>> lengths(AssemblyEntity assemblyEntity) throws IOException {
        List<Integer> lengthsList = assemblyEntity.getChromosomes().stream()
                                                  .map(chromosome -> Math.toIntExact(chromosome.getSeqLength()))
                                                  .toList();
        return lengths(lengthsList);
    }

    /**
     * Return the seqCol lengths array object from the parsed FASTA file
     */
    public SeqColExtendedDataEntity<List<Integer>> lengths(AssemblySequenceEntity sequenceEntity) throws IOException {
        return lengths(sequenceEntity.getSequences().stream().map(SeqColSequenceEntity::getLength).toList());
    }

    /**
     * Return the seqCol sequences (refget digests) array object
     */
    public SeqColExtendedDataEntity<List<String>> sequences(AssemblySequenceEntity assemblySequenceEntity)
            throws IOException {
        return stringListAttribute(AttributeType.sequences, assemblySequenceEntity.getSequences().stream()
                                                                               .map(SeqColSequenceEntity::getSequence)
                                                                               .toList());
    }

    /**
     * Return the seqCol md5 sequences array object
     */
    public SeqColExtendedDataEntity<List<String>> md5Sequences(AssemblySequenceEntity assemblySequenceEntity)
            throws IOException {
        return stringListAttribute(AttributeType.md5DigestsOfSequences, assemblySequenceEntity.getSequences().stream()
                                                                                           .map(SeqColSequenceEntity::getSequenceMD5)
                                                                                           .toList());
    }

    /**
     * Return the seqCol sorted-name-length-pairs extended object
     * @throws IllegalArgumentException if names and lengths don't have the same number of elements
     */
    public SeqColExtendedDataEntity<List<String>> sortedNameLengthPairs(
            SeqColExtendedDataEntity<List<String>> extendedNames, SeqColExtendedDataEntity<List<Integer>> extendedLengths)
            throws IOException {
        return sortedNameLengthPairs(extendedNames.getExtendedSeqColData().getObject(),
                                     extendedLengths.getExtendedSeqColData().getObject());
    }

    /**
     * Return the complete set of extended data: the sorted-name-length-pairs are derived from names and lengths.
     */
    public SeqColExtendedDataSet extendedDataSet(SeqColExtendedDataEntity<List<String>> sequences,
                                                 SeqColExtendedDataEntity<List<String>> md5Sequences,
                                                 SeqColExtendedDataEntity<List<String>> names,
                                                 SeqColExtendedDataEntity<List<Integer>> lengths) throws IOException {
        return new SeqColExtendedDataSet(sequences, md5Sequences, names, lengths, sortedNameLengthPairs(names, lengths));
    }

    /**
     * Return the complete set of extended data for one naming convention of the given assembly
     */
    public SeqColExtendedDataSet extendedDataSet(AssemblyEntity assemblyEntity,
                                                 AssemblySequenceEntity assemblySequenceEntity,
                                                 NamingConvention convention) throws IOException {
        return extendedDataSet(sequences(assemblySequenceEntity), md5Sequences(assemblySequenceEntity),
                               namesByNamingConvention(assemblyEntity, convention), lengths(assemblyEntity));
    }

    /**
     * Return the complete set of extended data of the given level 2 object.
     * The sorted-name-length-pairs are taken from the level 2 object as they are.
     */
    public SeqColExtendedDataSet extendedDataSet(SeqColLevelTwoEntity levelTwoEntity) throws IOException {
        return new SeqColExtendedDataSet(
                stringListAttribute(AttributeType.sequences, levelTwoEntity.getSequences()),
                stringListAttribute(AttributeType.md5DigestsOfSequences, levelTwoEntity.getMd5DigestsOfSequences()),
                stringListAttribute(AttributeType.names, levelTwoEntity.getNames()),
                lengths(levelTwoEntity.getLengths()),
                stringListAttribute(AttributeType.sortedNameLengthPairs, levelTwoEntity.getSortedNameLengthPairs()));
    }

    /**
     * Return the sorted-name-length-pair list (digest of each name-length pair, sorted) for the given pairs
     */
    public List<String> sortedNameLengthPairDigests(List<NameLengthPairEntity> nameLengthPairList)
            throws IOException {
        List<String> sortedNameLengthPairs = new ArrayList<>();
        for (NameLengthPairEntity entity : nameLengthPairList) {
            sortedNameLengthPairs.add(digestCalculator.getSha512Digest(entity.toString()));
        }
        // Sorting the name-length-pair list according to the elements' natural order (alphanumerically)
        sortedNameLengthPairs.sort((o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
        return sortedNameLengthPairs;
    }

    private SeqColExtendedDataEntity<List<String>> sortedNameLengthPairs(List<String> names, List<Integer> lengths)
            throws IOException {
        if (names.size() != lengths.size()) {
            throw new IllegalArgumentException("Names (" + names.size() + ") and lengths (" + lengths.size() +
                                                       ") must have the same number of elements");
        }
        List<NameLengthPairEntity> nameLengthPairList = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            nameLengthPairList.add(new NameLengthPairEntity(names.get(i), lengths.get(i)));
        }
        return stringListAttribute(AttributeType.sortedNameLengthPairs, sortedNameLengthPairDigests(nameLengthPairList));
    }

    private SeqColExtendedDataEntity<List<String>> names(List<String> namesList, NamingConvention convention)
            throws IOException {
        SeqColExtendedDataEntity<List<String>> names = stringListAttribute(AttributeType.names, namesList);
        names.setNamingConvention(convention);
        return names;
    }

    private SeqColExtendedDataEntity<List<Integer>> lengths(List<Integer> lengthsList) throws IOException {
        return attribute(AttributeType.lengths, new JSONIntegerListExtData(lengthsList));
    }

    private SeqColExtendedDataEntity<List<String>> stringListAttribute(AttributeType attributeType, List<String> values)
            throws IOException {
        return attribute(attributeType, new JSONStringListExtData(values));
    }

    private <T> SeqColExtendedDataEntity<T> attribute(AttributeType attributeType, JSONExtData<T> values)
            throws IOException {
        SeqColExtendedDataEntity<T> entity = new SeqColExtendedDataEntity<T>()
                .setAttributeType(attributeType)
                .setExtendedSeqColData(values);
        entity.setDigest(digestService.calculateAttributeDigest(values));
        return entity;
    }
}
