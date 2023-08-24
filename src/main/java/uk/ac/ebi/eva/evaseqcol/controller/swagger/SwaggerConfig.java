package uk.ac.ebi.eva.evaseqcol.controller.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.PathProvider;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.Paths;
import springfox.documentation.spring.web.plugins.Docket;

@OpenAPIDefinition(
        info = @Info(
                title = "Code-First Approach (reflectoring.io)",
                description = "" +
                        "Lorem ipsum dolor ...",
                contact = @Contact(
                        name = "Reflectoring",
                        url = "https://reflectoring.io",
                        email = "petros.stergioulas94@gmail.com"
                ),
                license = @License(
                        name = "MIT Licence",
                        url = "https://github.com/thombergs/code-examples/blob/master/LICENSE")),
        servers = @Server(url = "http://localhost:8080")
)
@Configuration
public class SwaggerConfig {

    private final String CONTROLLER_BASE_PATH = "uk.ac.ebi.eva.evaseqcol.controller";

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public Docket publicAPi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1/seqcol")
                .pathProvider(getPathProvider())
                //.apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PATH + ".seqcol"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("v1/only-admin")
                .pathProvider(getPathProvider())
                //.apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(CONTROLLER_BASE_PATH + ".admin"))
                .paths(PathSelectors.any())
                .build();
    }

    private PathProvider getPathProvider() {
        return new PathProvider() {
            @Override
            public String getOperationPath(String operationPath) {
                if (operationPath.startsWith(contextPath)) {
                    operationPath = operationPath.substring(contextPath.length());
                }
                return Paths.removeAdjacentForwardSlashes(
                        UriComponentsBuilder.newInstance().replacePath(operationPath).build().toString());
            }

            @Override
            public String getResourceListingPath(String groupName, String apiDeclaration) {
                return null;
            }
        };
    }

    /**
     * Return the swagger page's description
    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("Contig-Alias API")
                .description(
                        "Service to provide synonyms of chromosome/contig identifiers." +
                                "\nThe endpoints in the following controllers are paginated, which means that all " +
                                "results aren't returned at once, instead a small subset is returned. This small " +
                                "subset in the form of a page and the result need to be traversed through this set of" +
                                " pages." +
                                "\nYou can control pagination by specifying the index and size of the page you want " +
                                "using the two optional parameters \"page\" and \"size\" while querying the desired " +
                                "endpoint." +
                                "\nThe endpoints in the following controllers also provided hyperlinks to other " +
                                "relevant endpoints to help the user navigate the API with ease. These links are " +
                                "embedded inside an object called \"_links\" which is present at the root level of " +
                                "the response." +
                                "\nSome information about pagination is also similarly included in a root level " +
                                "object called \"page\". Due to this, the actual result is not available at the root " +
                                "level but is actually embedded in another root level element.")
                .version("1.0")
                .contact(new Contact("GitHub Repository", "https://github.com/EBIvariation/contig-alias", null))
                .license("Apache-2.0")
                .licenseUrl("https://raw.githubusercontent.com/EBIvariation/contig-alias/master/LICENSE")
                .build();
    }*/
}
