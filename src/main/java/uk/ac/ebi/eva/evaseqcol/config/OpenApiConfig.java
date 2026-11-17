package uk.ac.ebi.eva.evaseqcol.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    /**
     * Advertise a relative server URL so Swagger UI calls the API using the scheme and host the page was loaded from.
     */
    @Bean
    public OpenAPI seqColOpenAPI(@Value("${server.servlet.context-path:/}") String contextPath) {
        return new OpenAPI()
                .servers(List.of(new Server().url(contextPath)))
                .info(new Info().title("Sequence Collections API")
                        .description("A service that provides a standardized way to identify sequence collections." +
                                "\nThe endpoints of this API provide a service for ingestion of seqCol" +
                                "objects, a service for the retrieval of seqCol objects given their " +
                                "level 0 digests and a service for the comparison of two seqCol objects.")
                        .version("v1.0")
                        .license(new License().name("Apache-2.0").url("https://raw.githubusercontent.com/EBIvariation/eva-seqcol/main/LICENSE"))
                        .contact(new Contact().name("GitHub Repository").url("https://github.com/EBIvariation/eva-seqcol").email(null)));
    }
}
