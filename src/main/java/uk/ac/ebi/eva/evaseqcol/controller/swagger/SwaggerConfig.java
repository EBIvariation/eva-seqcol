package uk.ac.ebi.eva.evaseqcol.controller.swagger;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class SwaggerConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private SwaggerInterceptAdapter interceptAdapter;

    @Bean
    public OpenAPI seqColOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Sequence Collections API")
                              .description("A service that provides a standardized way to identify sequence collections." +
                                                   "\nThe endpoints of this API provide a service for ingestion of seqCol" +
                                                   "objects, a service for the retrieval of seqCol objects given their " +
                                                   "level 0 digests and a service for the comparison of two seqCol objects.")
                              .version("v1.0")
                              .license(new License().name("Apache-2.0").url("https://raw.githubusercontent.com/EBIvariation/contig-alias/master/LICENSE"))
                              .contact(new Contact().name("GitHub Repository").url("https://github.com/EBIvariation/eva-seqcol").email(null)));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptAdapter);
    }
}
