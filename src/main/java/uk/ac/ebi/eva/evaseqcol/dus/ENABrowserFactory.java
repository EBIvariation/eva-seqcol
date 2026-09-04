package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.stereotype.Component;

@Component
public class ENABrowserFactory {

    public ENABrowser build(){
        return new ENABrowser();
    }
}
