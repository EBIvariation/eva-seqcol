package uk.ac.ebi.eva.evaseqcol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColMetadataEntity;
import uk.ac.ebi.eva.evaseqcol.repo.MetadataRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;

    @Autowired
    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public List<SeqColMetadataEntity> getMetadataBySeqColDigest(String seqColDigest) {
        return metadataRepository.findAll().stream()
                                 .filter(metadataEntity -> metadataEntity.getSeqColLevelOne().getDigest().equals(seqColDigest))
                                 .collect(Collectors.toList());
    }
    /**
     * Return the list of all saved source identifiers. Eg: ["GCA_000146045.2", ...]
     * */
    public List<String> getAllSourceIds() {
        return metadataRepository.findAllSourceIds();
    }
}
