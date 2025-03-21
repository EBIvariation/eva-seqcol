package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.evaseqcol.dto.PaginatedResponse;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColExtendedDataEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelOneEntity;
import uk.ac.ebi.eva.evaseqcol.entities.SeqColLevelTwoEntity;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.UnableToLoadServiceInfoException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@Tag(name = "Seqcol endpoints")
public class SeqColController {
    private static final Logger logger = LoggerFactory.getLogger(SeqColController.class);

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
            example = "1") @RequestParam(required = false) String level,
            @Parameter(name = "metadata",
            description = "A boolean value that indicates if we need the metadata of the given seqcol digest",
            example = "true, 1, yes")
            @RequestParam(required = false, defaultValue = "false") boolean metadata) {
        if (metadata) {
            return new ResponseEntity<>(
                    seqColService.getSeqColMetadataBySeqColDigest(digest), HttpStatus.OK
            );
        }
        if (level == null) level = "none";
        try {
            switch (level) {
                case "1":
                    Optional<SeqColLevelOneEntity> levelOneEntity = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestLevel1(digest);
                    if (levelOneEntity.isPresent()) {
                        return ResponseEntity.ok(levelOneEntity.get());
                    }
                    break;
                case "2":
                case "none":
                    Optional<SeqColLevelOneEntity> optionalSeqColLevelOne = (Optional<SeqColLevelOneEntity>) seqColService.getSeqColByDigestLevel1(digest);
                    if (!optionalSeqColLevelOne.isPresent()) {
                        logger.warn("No seqCol corresponding to digest " + digest + " could be found in the db");
                        throw new SeqColNotFoundException(digest);
                    }
                    Optional<SeqColLevelTwoEntity> levelTwoEntity = (Optional<SeqColLevelTwoEntity>) seqColService.getSeqColByDigestLevel2(optionalSeqColLevelOne.get());
                    if (levelTwoEntity.isPresent()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("seqColLevel2Object", levelTwoEntity.get());
                        response.put("metadata", optionalSeqColLevelOne.get().getMetadata());
                        return ResponseEntity.ok(response);
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

    @GetMapping("/list/collection")
    public ResponseEntity<?> getList(@RequestParam Map<String, String> queryParams) {

        int page = Integer.parseInt(queryParams.getOrDefault("page", "0"));
        int pageSize = Integer.parseInt(queryParams.getOrDefault("page_size", "10"));

        Map<String, String> filters = queryParams.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("page_size"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        PaginatedResponse<String> results = seqColService.getSeqColList(page, pageSize, filters);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/attribute/collection/{attribute}/{digest}")
    public ResponseEntity<?> getAttribute(@Parameter(name = "attribute",
                                                  description = "name of the attribute e.g. lengths, names",
                                                  example = "names",
                                                  required = true)
                                          @PathVariable SeqColExtendedDataEntity.AttributeType attribute,
                                          @Parameter(name = "digest",
                                                  description = "SeqCol's level 0 digest",
                                                  example = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq",
                                                  required = true) @PathVariable String digest) {
        try {
            Optional<List<String>> optionalSeqColAttributeValue = seqColService.getSeqColAttribute(digest, attribute);
            if (optionalSeqColAttributeValue.isPresent()) {
                return ResponseEntity.ok(optionalSeqColAttributeValue.get());
            } else {
                return new ResponseEntity<>("Could not find " + attribute.name() + " with digest " + digest, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
