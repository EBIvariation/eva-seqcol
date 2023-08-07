package uk.ac.ebi.eva.evaseqcol.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.io.IOException;
import java.util.List;

@RequestMapping("/collection/admin")
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
    @PutMapping(value = "/seqcols/{asmAccession}")
    public ResponseEntity<?> fetchAndInsertSeqColByAssemblyAccession(
            @PathVariable String asmAccession) {
        try {
            List<String> level0Digests = seqColService.fetchAndInsertAllSeqColByAssemblyAccession(asmAccession);
            return new ResponseEntity<>(
                    "Successfully inserted seqCol object(s) for assembly accession " + asmAccession + "\nSeqCol digests=" + level0Digests
                    , HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DuplicateSeqColException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        } catch (AssemblyNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
