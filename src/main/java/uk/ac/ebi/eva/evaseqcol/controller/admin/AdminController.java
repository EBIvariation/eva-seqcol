package uk.ac.ebi.eva.evaseqcol.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.exception.IncorrectAccessionException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.io.IOException;
import java.util.List;

@RequestMapping("/admin")
@RestController
public class AdminController {

    private SeqColService seqColService;

    @Autowired
    public AdminController(SeqColService seqColService) {
        this.seqColService = seqColService;
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
            @ApiResponse(responseCode = "200", description = "seqCol object(s) successfully inserted"),
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
            required = true) @PathVariable String asmAccession) {
        try {
            List<String> level0Digests = seqColService.fetchAndInsertAllSeqColByAssemblyAccession(asmAccession);
            return new ResponseEntity<>(
                    "Successfully inserted seqCol object(s) for assembly accession " + asmAccession + "\nSeqCol digests=" + level0Digests
                    , HttpStatus.OK);
        } catch (IncorrectAccessionException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DuplicateSeqColException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (AssemblyNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
