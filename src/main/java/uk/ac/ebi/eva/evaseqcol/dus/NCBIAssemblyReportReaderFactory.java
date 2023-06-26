package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class NCBIAssemblyReportReaderFactory {

    @Value("${config.scaffolds.enabled:false}")
    private boolean SCAFFOLDS_ENABLED;

    public NCBIAssemblyReportReader build(InputStream inputStream) {
        return new NCBIAssemblyReportReader(new InputStreamReader(inputStream), SCAFFOLDS_ENABLED);
    }

    public NCBIAssemblyReportReader build(InputStreamReader inputStreamReader) {
        return new NCBIAssemblyReportReader(inputStreamReader, SCAFFOLDS_ENABLED);
    }
}
