package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.Optional;

@RestController
@RequestMapping("/collection")
public class SeqColController {

    private SeqColService seqColService;

    @Autowired
    public SeqColController(SeqColService seqColService) {
        this.seqColService = seqColService;
    }

    @GetMapping(value = "/{digest}")
    public ResponseEntity<SeqColEntity> getSeqColByDigestAndLevel(
            @PathVariable String digest, @RequestParam(required = false) String level) {
        if (level == null) {
            level = "none";
        }
        switch (level) {
            case "1":
            case "none":
                Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestAndLevel(digest, 1);
                if (levelOneEntity.isPresent()) {
                    return ResponseEntity.ok(levelOneEntity.get());
                }
                break;
            case "2":
                Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestAndLevel(digest, 2);
                if (levelTwoEntity.isPresent()) {
                    return ResponseEntity.ok(levelTwoEntity.get());
                }
                break;
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
