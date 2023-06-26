package uk.ac.ebi.eva.evaseqcol.dus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ENABrowserFactory {

    @Value("${ftp.proxy.host}")
    private String ftpProxyHost;

    @Value("${ftp.proxy.port}")
    private Integer ftpProxyPort;

    public ENABrowser build(){
        return new ENABrowser(ftpProxyHost, ftpProxyPort);
    }
}
