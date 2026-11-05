package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.ac.ebi.eva.evaseqcol.entities.AttributeType;

@RestController
@RequestMapping("/")
@Tag(name = "Seqcol endpoints")
public class SeqColController {
    private final SeqColService seqColService;

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
            example = "1") @RequestParam(required = false) String level,
            @Parameter(name = "metadata",
            description = "A boolean value that indicates if we need the metadata of the given seqcol digest",
            example = "true, 1, yes")
            @RequestParam(required = false, defaultValue = "false") boolean metadata) {
        if (metadata) {
            return ResponseEntity.ok(seqColService.getSeqColMetadataBySeqColDigest(digest));
        }
        if (level == null || level.equals("2") || level.equals("none")) { // default level is 2
            return ResponseEntity.ok(seqColService.getSeqColByDigestLevel2(digest)
                                                  .orElseThrow(() -> new SeqColNotFoundException(digest)));
        }
        if (level.equals("1")) {
            return ResponseEntity.ok(seqColService.getSeqColByDigestLevel1(digest)
                                                  .orElseThrow(() -> new SeqColNotFoundException(digest))
                                                  .getSeqColLevel1Object());
        }
        return new ResponseEntity<>("Level should be either 1 or 2", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/list/collection")
    public ResponseEntity<?> getList(@RequestParam Map<String, String> queryParams) {

        int page = Integer.parseInt(queryParams.getOrDefault("page", "0"));
        int pageSize = Integer.parseInt(queryParams.getOrDefault("page_size", "10"));

        Map<String, String> filters = new HashMap<>(queryParams);
        filters.remove("page");
        filters.remove("page_size");
        return ResponseEntity.ok(seqColService.getSeqColList(page, pageSize, filters));
    }

    @GetMapping("/attribute/collection/{attributeValue}/{digest}")
    public ResponseEntity<?> getAttribute(@Parameter(name = "attribute",
                                                  description = "name of the attribute e.g. lengths, names",
                                                  example = "names",
                                                  required = true)
                                          @PathVariable String attributeValue,
                                          @Parameter(name = "digest",
                                                  description = "SeqCol's level 1 digest",
                                                  example = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq",
                                                  required = true) @PathVariable String digest) {
        AttributeType attribute = AttributeType.fromAttributeVal(attributeValue);
        Optional<List<?>> attributeValues = seqColService.getSeqColAttribute(digest, attribute);
        if (attributeValues.isPresent()) {
            return ResponseEntity.ok(attributeValues.get());
        }
        return new ResponseEntity<>("Could not find " + attributeValue + " with digest " + digest, HttpStatus.NOT_FOUND);
    }


    @GetMapping("/service-info")
    public ResponseEntity<?> getServiceInfo() {
        return ResponseEntity.ok(seqColService.getServiceInfo());
    }
}
