package uk.ac.ebi.eva.evaseqcol.controller.seqcol;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColComparisonResultEntity;
import uk.ac.ebi.eva.evaseqcol.exception.SeqColNotFoundException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.util.List;
import java.util.TreeMap;

@RestController
@RequestMapping("/comparison")
public class SeqColComparisonController {

    private SeqColService seqColService;

    @Autowired
    public SeqColComparisonController(SeqColService seqColService) {
        this.seqColService = seqColService;
    }

    @Operation(summary = "Compare two local sequence collection objects",
    description = "Given two seqCol's level 0 digests, this endpoint will try to fetch the two corresponding seqCol " +
            "objects from the database, compare them and give back the result of this comparison as defined in " +
            "https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#2022-06-15---structure-for-the-return-value-of-the-comparison-api-endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparison has completed successfully"),
            @ApiResponse(responseCode = "404", description = "One of the compared seqCol object was not found"),
            @ApiResponse(responseCode = "500", description = "Server error. Maybe it's related to the seqCol Map fields")
    })
    @GetMapping("/{digest1}/{digest2}")
    public ResponseEntity<?> compareSequenceCollections(
            @Parameter(name = "digest1",
                    description = "Level 0 digest of seqColA",
                    example = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq",
                    required = true

            ) @PathVariable String digest1,@Parameter(name = "digest2",
            description = "Level 0 digest of seqColB",
            example = "rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE",
            required = true

    ) @PathVariable String digest2) {
        try {
            SeqColComparisonResultEntity comparisonResult = seqColService.compareSeqCols(digest1, digest2);
            return new ResponseEntity<>(comparisonResult, HttpStatus.OK);
        } catch (NoSuchFieldException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (SeqColNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Compare a local sequence collection object with a provided one",
    description = "Given a seqCol's level 0 digest and a JSON representation of another seqCol level 2 object provided" +
            "in the POST request's body,this endpoint will try to fetch the seqCol object with the given digest " +
            "from the db, compare it with the provided one and give back the result of this comparison as defined in " +
            "https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#2022-06-15---structure-for-the-return-value-of-the-comparison-api-endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparison has completed successfully"),
            @ApiResponse(responseCode = "404", description = "Could not find a seqCol object with the given digest"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PostMapping("/{digest1}")
    public ResponseEntity<?> compareSequenceCollections(
            @Parameter(name = "digest1",
            description = "Level 0 digest of seqColA",
            example = "3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq",
            required = true) @PathVariable String digest1,
            @Parameter(name = "seqColLevelTwo",
            description = "SeqCol object level 2",
            required = true) @RequestBody TreeMap<String, List<?>> seqColLevelTwo
    ) {
        try {
            SeqColComparisonResultEntity comparisonResult = seqColService.compareSeqCols(digest1, seqColLevelTwo);
            return new ResponseEntity<>(comparisonResult, HttpStatus.OK);
        } catch (SeqColNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
