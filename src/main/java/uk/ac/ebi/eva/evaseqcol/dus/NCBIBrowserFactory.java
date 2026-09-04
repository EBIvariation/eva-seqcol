package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.stereotype.Component;

@Component
public class NCBIBrowserFactory {
    public NCBIBrowser build(){
        return new NCBIBrowser();
    }
}
