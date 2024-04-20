package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.dto.SeqColMetadataDTO;
import uk.ac.ebi.eva.evaseqcol.service.MetadataService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/metadata/")
@Tag(name = "Seqcol metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping(value = "/{digest}")
    public ResponseEntity<List<SeqColMetadataDTO>> getSeqColMetadataByDigest(@PathVariable String digest) {
        List<SeqColMetadataDTO> metadataList = metadataService.getMetadataBySeqColDigest(digest).stream()
                .map(SeqColMetadataDTO::toMetadataDTO)
                .collect(Collectors.toList());
        if (metadataList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadataList);
    }
}
