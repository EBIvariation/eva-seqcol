package uk.ac.ebi.eva.evaseqcol.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.evaseqcol.entities.SeqColEntity;
import uk.ac.ebi.eva.evaseqcol.exception.DuplicateSeqColException;
import uk.ac.ebi.eva.evaseqcol.service.SeqColService;

import java.io.IOException;
import java.util.Optional;

@RequestMapping("/collection/admin")
@RestController
public class AdminController {

    private SeqColService seqColService;

    @Autowired
    public AdminController(SeqColService seqColService) {
        this.seqColService = seqColService;
    }

    /**
     * Naming convention should be either ENA, GENBANK or UCSC */
    @PutMapping(value = "/seqcols/{asmAccession}/{namingConvention}")
    public ResponseEntity<?> fetchAndInsertSeqColByAssemblyAccessionAndNamingConvention(
            @PathVariable String asmAccession, @PathVariable String namingConvention) {
        // TODO: REMOVE THE NAMING CONVENTION PATH VARIABLE AND MAKE IT GENERIC
        try {
            Optional<String> level0Digest = seqColService.fetchAndInsertSeqColByAssemblyAccession(
                    asmAccession, SeqColEntity.NamingConvention.valueOf(namingConvention));
            return new ResponseEntity<>(
                    "Successfully inserted seqCol for assemblyAccession " + asmAccession + "\nDigest=" + level0Digest.get()
                    , HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DuplicateSeqColException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.OK);
        }
    }
}
