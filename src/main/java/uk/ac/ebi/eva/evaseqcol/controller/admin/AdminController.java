package uk.ac.ebi.eva.evaseqcol.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.eva.evaseqcol.service.SeqColIngestionService;

import java.io.IOException;

@RequestMapping("/admin")
@RestController
@Tag(name = "Admin endpoints")
public class AdminController {

    private final SeqColIngestionService ingestionService;

    public AdminController(SeqColIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Fetch and insert all possible seqCol objects given the assembly accession
     * NOTE: All possible means with all naming conventions that exist in the fetched assembly report*/
    @Operation(summary = "Add new sequence collection objects",
            description = "Given an INSDC or RefSeq accession, this endpoint will fetch the corresponding assembly " +
                    "report and the assembly sequences FASTA file from the NCBI datasource, use them to construct " +
                    "seqCol objects with as many naming conventions as possible (depends on the naming conventions " +
                    "contained in the assembly report) and eventually save these seqCol objects into the database. " +
                    "This is an authenticated endpoint, so it requires admin privileges to run it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "seqCol object(s) successfully inserted"),
            @ApiResponse(responseCode = "409", description = "seqCol object(s) already exist(s)"),
            @ApiResponse(responseCode = "404", description = "Assembly not found"),
            @ApiResponse(responseCode = "400", description = "Bad request. (It can be a bad accession value)"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PutMapping(value = "/seqcols/{asmAccession}")
    public ResponseEntity<?> fetchAndInsertSeqColByAssemblyAccession(
            @Parameter(name = "asmAccession",
            description = "INSDC or RefSeq assembly accession",
            example = "GCA_000146045.2",
            required = true) @PathVariable String asmAccession) throws IOException {
        return new ResponseEntity<>(ingestionService.fetchAndInsertAllSeqColByAssemblyAccession(asmAccession),
                                    HttpStatus.CREATED);
    }

    @Operation(summary = "Add new sequence collection objects",
            description = "Given FASTA file content, this endpoint will parse the content and use it to construct " +
                    "seqCol objects with naming convention TEST and eventually save these seqCol objects into the database. " +
                    "This is an authenticated endpoint, so it requires admin privileges to run it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "seqCol object(s) successfully inserted"),
            @ApiResponse(responseCode = "409", description = "seqCol object(s) already exist(s)"),
            @ApiResponse(responseCode = "404", description = "Assembly not found"),
            @ApiResponse(responseCode = "400", description = "Bad request. (It can be a bad accession value)"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @PutMapping(value = "/seqcols/fasta/{accession}")
    public ResponseEntity<?> fetchAndInsertSeqColByParsingFastaFile(@PathVariable(value = "accession") String accession, @RequestBody String fastaFileContent) throws IOException {
        return new ResponseEntity<>(ingestionService.fetchAndInsertAllSeqColInFastaFile(accession, fastaFileContent),
                                    HttpStatus.CREATED);
    }
}
