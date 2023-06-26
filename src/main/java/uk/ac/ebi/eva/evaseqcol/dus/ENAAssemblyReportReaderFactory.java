package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class ENAAssemblyReportReaderFactory {

    @Value("${config.scaffolds.enabled:false}")
    private boolean SCAFFOLDS_ENABLED;

    public ENAAssemblyReportReader build(InputStream inputStream) {
        return new ENAAssemblyReportReader(new InputStreamReader(inputStream), SCAFFOLDS_ENABLED);
    }

    public ENAAssemblyReportReader build(InputStreamReader inputStreamReader) {
        return new ENAAssemblyReportReader(inputStreamReader, SCAFFOLDS_ENABLED);
    }
}
