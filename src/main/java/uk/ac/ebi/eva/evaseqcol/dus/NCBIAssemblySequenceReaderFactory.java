package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class NCBIAssemblySequenceReaderFactory {

    public NCBIAssemblySequenceReader build(InputStream inputStream, String accession){
        return new NCBIAssemblySequenceReader(new InputStreamReader(inputStream), accession);
    }

    public NCBIAssemblySequenceReader build(InputStreamReader inputStreamReader, String accession){
        return new NCBIAssemblySequenceReader(inputStreamReader, accession);
    }
}
