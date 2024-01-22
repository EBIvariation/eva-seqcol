package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.UnableToLoadServiceInfoException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/")
@Tag(name = "Secqol endpoint")
public class SeqColController {

    private SeqColService seqColService;

    @Autowired
    public SeqColController(SeqColService seqColService) {
        this.seqColService = seqColService;
    }

    @Operation(summary = "Retrieve seqCol object by digest",
    description = "Given a seqCol's level 0 digest, this endpoint will try to fetch the corresponding seqCol object from" +
            " the database and return it in the specified level. The default level value is 2.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SeqCol was returned successfully"),
            @ApiResponse(responseCode = "404", description = "Could not find a seqCol object with the given digest"),
            @ApiResponse(responseCode = "400", description = "Not valid level value"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping(value = "/collection/{digest}")
    public ResponseEntity<?> getSeqColByDigestAndLevel(
            @Parameter(name = "digest",
            description = "SeqCol's level 0 digest",
            example = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq",
            required = true) @PathVariable String digest,
            @Parameter(name = "level",
            description = "The desired output's level (1 or 2)",
            example = "1") @RequestParam(required = false) String level) {
        if (level == null) level = "none";
        try {
            switch (level) {
                case "1":
                    Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestAndLevel(digest, 1);
                    if (levelOneEntity.isPresent()) {
                        return ResponseEntity.ok(levelOneEntity.get().getSeqColLevel1Object());
                    }
                    break;
                case "2":
                case "none":
                    Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestAndLevel(digest, 2);
                    if (levelTwoEntity.isPresent()) {
                        return ResponseEntity.ok(levelTwoEntity.get());
                    }
                    break;
                default:
                    // Not a valid level value
                    return new ResponseEntity<>("Level should be either 1 or 2", HttpStatus.BAD_REQUEST);
            }
        } catch (SeqColNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/service-info")
    public ResponseEntity<?> getServiceInfo() {
        try {
            Map<String, Object> serviceInfoMap = seqColService.getServiceInfo();
            return new ResponseEntity<>(serviceInfoMap, HttpStatus.OK);
        } catch (UnableToLoadServiceInfoException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
