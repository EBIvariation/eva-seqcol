package uk.ac.ebi.eva.evaseqcol.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RootRedirectInterceptor rootRedirectInterceptor;

    public WebMvcConfig(RootRedirectInterceptor rootRedirectInterceptor) {
        this.rootRedirectInterceptor = rootRedirectInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rootRedirectInterceptor);
    }
}
