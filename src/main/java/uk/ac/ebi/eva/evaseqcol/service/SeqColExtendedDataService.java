package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.refget.DigestCalculator;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColExtendedDataRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class SeqColExtendedDataService {

    @Autowired
    private SeqColExtendedDataRepository repository;

    private DigestCalculator digestCalculator = new DigestCalculator();

    /**
     * Add a seqCol's attribute; names, lengths or sequences, to the database*/
    public Optional<SeqColExtendedDataEntity> addSeqColExtendedData(SeqColExtendedDataEntity seqColExtendedData){
        try {
            SeqColExtendedDataEntity seqCol = repository.save(seqColExtendedData);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Transactional
    public List<SeqColExtendedDataEntity> addAll(List<SeqColExtendedDataEntity> seqColExtendedDataList) {
        return repository.saveAll(seqColExtendedDataList);
    }

    /**
     * Return the extendedData object for the given digest*/
    Optional<SeqColExtendedDataEntity> getExtendedAttributeByDigest(String digest) {
        SeqColExtendedDataEntity dataEntity = repository.findSeqColExtendedDataEntityByDigest(digest);
        return Optional.of(dataEntity);
    }


    /**
     * Return the seqCol names array object*/
    SeqColExtendedDataEntity constructSeqColNamesObject(AssemblyEntity assemblyEntity, SeqColEntity.NamingConvention convention) throws IOException {
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

        seqColNamesArray.setObject(namesList);
        seqColNamesObject.setObject(seqColNamesArray);
        seqColNamesObject.setDigest(digestCalculator.generateDigest(seqColNamesArray.toString()));
        return seqColNamesObject;
    }

    /**
     * Return the seqCol lengths array object*/
    public SeqColExtendedDataEntity constructSeqColLengthsObject(AssemblyEntity assemblyEntity) throws IOException {
        SeqColExtendedDataEntity seqColLengthsObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.lengths);
        JSONExtData seqColLengthsArray = new JSONExtData();
        List<String> lengthsList = new LinkedList<>();

        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            lengthsList.add(chromosome.getSeqLength().toString());
        }
        seqColLengthsArray.setObject(lengthsList);
        seqColLengthsObject.setObject(seqColLengthsArray);
        seqColLengthsObject.setDigest(digestCalculator.generateDigest(seqColLengthsArray.toString()));
        return seqColLengthsObject;
    }

    /**
     * Return the seqCol sequences array object*/
    public SeqColExtendedDataEntity constructSeqColSequencesObject(AssemblySequenceEntity assemblySequenceEntity) throws IOException {
        SeqColExtendedDataEntity seqColSequencesObject = new SeqColExtendedDataEntity().setAttributeType(
                SeqColExtendedDataEntity.AttributeType.sequences);
        JSONExtData seqColSequencesArray = new JSONExtData();
        List<String> sequencesList = new LinkedList<>();

        for (SeqColSequenceEntity sequence: assemblySequenceEntity.getSequences()) {
            sequencesList.add(sequence.getSequenceMD5());
        }
        seqColSequencesArray.setObject(sequencesList);
        seqColSequencesObject.setObject(seqColSequencesArray);
        seqColSequencesObject.setDigest(digestCalculator.generateDigest(seqColSequencesArray.toString()));
        return seqColSequencesObject;
    }

    /**
     * Return the 3 extended data objects (names, lengths and sequences) of the given naming convention*/
    List<SeqColExtendedDataEntity> constructExtendedSeqColDataList(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                            SeqColEntity.NamingConvention convention, String assemblyAccession) throws IOException {
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        sortReportAndSequencesBySequenceIdentifier(assemblyEntity, assemblySequenceEntity, assemblyAccession);
        return Arrays.asList(
                constructSeqColSequencesObject(assemblySequenceEntity),
                constructSeqColNamesObject(assemblyEntity, convention),
                constructSeqColLengthsObject(assemblyEntity)
        );
    }

    /**
     * Construct and return a Level Two (with exploded data) SeqCol entity out of the given assemblyEntity and the
     * assemblySequencesEntity*/
    SeqColLevelTwoEntity constructSeqColLevelTwo(AssemblyEntity assemblyEntity, AssemblySequenceEntity assemblySequenceEntity,
                                                 SeqColEntity.NamingConvention convention, String accession) throws IOException {
        SeqColLevelTwoEntity seqColLevelTwo = new SeqColLevelTwoEntity();
        sortReportAndSequencesBySequenceIdentifier(assemblyEntity, assemblySequenceEntity, accession);
        SeqColExtendedDataEntity extendedNamesData = constructSeqColNamesObject(assemblyEntity, convention);
        SeqColExtendedDataEntity extendedLengthsData = constructSeqColLengthsObject(assemblyEntity);
        SeqColExtendedDataEntity extendedSequencesData = constructSeqColSequencesObject(assemblySequenceEntity);
        seqColLevelTwo.setNames(extendedNamesData.getObject().getObject());
        seqColLevelTwo.setLengths(extendedLengthsData.getObject().getObject());
        seqColLevelTwo.setSequences(extendedSequencesData.getObject().getObject());
        return seqColLevelTwo;
    }

    /**
     * Sort the chromosome list of the assemblyEntity and the sequences list of the assemblySequenceEntity
     * by the sequence identifier */
    void sortReportAndSequencesBySequenceIdentifier(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                                    String accession) {
        String accessionType = getAccessionType(accession);

        Comparator<ChromosomeEntity> chromosomeComparator = (o1, o2) -> {
            String identifier1 = new String();
            String identifier2 = new String();
            switch (accessionType) {
                case "GCF":
                    identifier1 = o1.getRefseq();
                    identifier2 = o2.getRefseq();
                    break;
                case "GCA":
                    identifier1 = o1.getInsdcAccession();
                    identifier2 = o2.getInsdcAccession();
                    break;
            }

            String substring1 = identifier1.substring(identifier1.indexOf(".") + 1, identifier1.length());
            String substring2 = identifier2.substring(identifier2.indexOf(".") + 1, identifier2.length());
            if (!substring1.equals(substring2))
                return substring1.compareTo(substring2);
            return identifier1.substring(0,identifier1.indexOf(".")).compareTo(identifier2.substring(0,identifier2.indexOf(".")));
        };
        Collections.sort(assemblyEntity.getChromosomes(), chromosomeComparator);
        Comparator<SeqColSequenceEntity> sequenceComparator = (o1, o2) -> {
            String identifier = o1.getRefseq();
            String identifier1 = o2.getRefseq();
            String substring1 = identifier.substring(identifier.indexOf(".") + 1, identifier.length());
            String substring2 = identifier1.substring(identifier1.indexOf(".") + 1, identifier1.length());
            if (!substring1.equals(substring2))
                return substring1.compareTo(substring2);
            return identifier.substring(0,identifier.indexOf(".")).compareTo(identifier1.substring(0,identifier1.indexOf(".")));
        };
        Collections.sort(sequenceEntity.getSequences(), sequenceComparator);

    }

    /**
     * Return "GCF" (INSDC: GenBank) or "GCA" (Refseq) depending on the given accession */
    public String getAccessionType(String accession) {
        if (accession.startsWith("GCA"))
            return "GCA"; // INSDC: GenBank accession
        else
            return "GCF"; // Refseq accession
    }
}
