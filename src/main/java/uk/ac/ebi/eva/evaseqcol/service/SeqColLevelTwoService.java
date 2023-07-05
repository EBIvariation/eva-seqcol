package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColLevelTwoRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONLevelTwo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeqColLevelTwoService {

    @Autowired
    private SeqColLevelTwoRepository repository;

    public Optional<SeqColLevelTwoEntity> addSequenceCollectionL2(SeqColLevelTwoEntity seqColLevelTwo){
        try {
            SeqColLevelTwoEntity seqCol = repository.save(seqColLevelTwo);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            e.printStackTrace();
            //System.out.println("SeqcolL2 with digest " + seqColLevelTwo.getDigest() + " already exists in the db !!");

        }
        return Optional.empty();
    }

    @Transactional
    public List<SeqColLevelTwoEntity> addAll(List<SeqColLevelTwoEntity> seqColLevelTwoEntities) {
        return repository.saveAll(seqColLevelTwoEntities);
    }

    public List<SeqColLevelTwoEntity> getAll() {
        return repository.findAll();
    }

    /**
     * Return the 3 extracted seqcol objects (names, lengths and sequences) of the given naming convention*/
     public List<SeqColLevelTwoEntity> constructLevelTwoSeqCols(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                                        SeqColEntity.NamingConvention convention, String accession){
        // Sorting the chromosomes' list (assemblyEntity) and the sequences' list (sequencesEntity) in the same order
        sortReportAndSequencesByRefseq(assemblyEntity, sequenceEntity, accession);
        SeqColLevelTwoEntity namesEntity;
        SeqColLevelTwoEntity lengthsEntity;
        SeqColLevelTwoEntity sequencesEntity;
        JSONLevelTwo jsonNamesObject = new JSONLevelTwo();
        JSONLevelTwo jsonLengthsObject = new JSONLevelTwo();
        JSONLevelTwo jsonSequencesObject = new JSONLevelTwo();
        List<String> sequencesNamesObject = new LinkedList<>(); // Array of sequences' names
        List<String> sequencesLengthsObject = new LinkedList<>(); // Array of sequences' lengths
        List<String> sequencesObject = new LinkedList<>(); // // Array of actual sequences



        // Setting the sequences' names
        for (SequenceEntity chromosome: assemblyEntity.getChromosomes()) {
            switch (convention) {
                case ENA:
                    sequencesNamesObject.add(chromosome.getEnaSequenceName());
                    break;
                case GENBANK:
                    sequencesNamesObject.add(chromosome.getGenbankSequenceName());
                    break;
                case UCSC:
                    sequencesNamesObject.add(chromosome.getUcscName());
                    break;
            }
            sequencesLengthsObject.add(chromosome.getSeqLength().toString());
        }

        // Setting actual sequences
        for (SeqColSequenceEntity sequence: sequenceEntity.getSequences()) {
            sequencesObject.add(sequence.getSequenceMD5());
        }

        jsonNamesObject.setObject(sequencesNamesObject);
        String namesDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesNamesObject AND PUT IT HERE
        namesEntity = new SeqColLevelTwoEntity().setObject(jsonNamesObject);
        namesEntity.setDigest(namesDigest);

        jsonLengthsObject.setObject(sequencesLengthsObject);
        String lengthsDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesLengthsObject AND PUT IT HERE
        lengthsEntity = new SeqColLevelTwoEntity().setObject(jsonLengthsObject);
        lengthsEntity.setDigest(lengthsDigest);

        jsonSequencesObject.setObject(sequencesObject);
        String sequencesDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesObject AND PUT IT HERE
        sequencesEntity = new SeqColLevelTwoEntity().setObject(jsonSequencesObject);
        sequencesEntity.setDigest(sequencesDigest);

        List<SeqColLevelTwoEntity> entities = new ArrayList<>(
                Arrays.asList(namesEntity, lengthsEntity, sequencesEntity)
        );
        return entities;
    }

    public void sortReportAndSequencesByRefseq(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
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
