package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ebi.eva.evaseqcol.entities.AssemblyEntity;
import uk.ac.ebi.eva.evaseqcol.entities.AssemblySequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.ChromosomeEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColSequenceEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SequenceEntity;
import uk.ac.ebi.eva.evaseqcol.repo.SeqColExtendedDataRepository;
import uk.ac.ebi.eva.evaseqcol.utils.JSONExtData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeqColExtendedDataService {

    @Autowired
    private SeqColExtendedDataRepository repository;

    public Optional<SeqColExtendedDataEntity> addSeqColExtendedData(SeqColExtendedDataEntity seqColExtendedData){
        try {
            SeqColExtendedDataEntity seqCol = repository.save(seqColExtendedData);
            return Optional.of(seqCol);
        } catch (Exception e){
            // TODO : THROW A SELF MADE EXCEPTION
            e.printStackTrace();
            //System.out.println("SeqcolL2 with digest " + seqColExtendedData.getDigest() + " already exists in the db !!");

        }
        return Optional.empty();
    }

    @Transactional
    public List<SeqColExtendedDataEntity> addAll(List<SeqColExtendedDataEntity> seqColExtendedDataList) {
        return repository.saveAll(seqColExtendedDataList);
    }

    public List<SeqColExtendedDataEntity> getAll() {
        return repository.findAll();
    }

    /**
     * Return the 3 extracted seqcol objects (names, lengths and sequences) of the given naming convention*/
     public List<SeqColExtendedDataEntity> constructLevelTwoSeqCols(AssemblyEntity assemblyEntity, AssemblySequenceEntity sequenceEntity,
                                                                    SeqColEntity.NamingConvention convention, String accession){
        SeqColExtendedDataEntity namesEntity;
        SeqColExtendedDataEntity lengthsEntity;
        SeqColExtendedDataEntity sequencesEntity;
        JSONExtData jsonNamesObject = new JSONExtData();
        JSONExtData jsonLengthsObject = new JSONExtData();
        JSONExtData jsonSequencesObject = new JSONExtData();
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
        namesEntity = new SeqColExtendedDataEntity().setExtendedSeqColData(jsonNamesObject);
        namesEntity.setDigest(namesDigest);

        jsonLengthsObject.setObject(sequencesLengthsObject);
        String lengthsDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesLengthsObject AND PUT IT HERE
        lengthsEntity = new SeqColExtendedDataEntity().setExtendedSeqColData(jsonLengthsObject);
        lengthsEntity.setDigest(lengthsDigest);

        jsonSequencesObject.setObject(sequencesObject);
        String sequencesDigest = UUID.randomUUID().toString(); // TODO: CALCULATE THE DIGEST OF THE sequencesObject AND PUT IT HERE
        sequencesEntity = new SeqColExtendedDataEntity().setExtendedSeqColData(jsonSequencesObject);
        sequencesEntity.setDigest(sequencesDigest);

        List<SeqColExtendedDataEntity> entities = new ArrayList<>(
                Arrays.asList(namesEntity, lengthsEntity, sequencesEntity)
        );
        return entities;
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
