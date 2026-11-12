package uk.ac.ebi.eva.evaseqcol.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
/**
 * Redirects the bare service roots (context path, /collection and /comparison) to the Swagger UI.
 */
public class RootRedirectInterceptor implements HandlerInterceptor {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String req = request.getRequestURI();

        if (req.equals(contextPath) || req.equals(contextPath + "/") ||
                req.equals(contextPath + "/collection") || req.equals(contextPath + "/collection/")
                || req.equals(contextPath + "/comparison") || req.equals(contextPath + "/comparison/")) {
            response.sendRedirect(contextPath + "/swagger-ui/index.html");
            return false;
        }

        return true;
    }
}
