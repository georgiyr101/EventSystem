package com.example.eventsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.List;

/**
 * Serves the Vite/React build from classpath:/static and falls back to {@code index.html}
 * for client-side routes. API, Actuator, and OpenAPI paths are excluded so they are not
 * masked by the SPA shell.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    private static final List<String> BACKEND_RESOURCE_PREFIXES = List.of(
            "api/",
            "actuator/",
            "v3/",
            "swagger-ui"
    );

    private static final Resource INDEX_HTML = new ClassPathResource("static/index.html");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource found = super.getResource(resourcePath, location);
                        if (found != null) {
                            return found;
                        }
                        if (isBackendResourcePath(resourcePath) || !INDEX_HTML.exists()) {
                            return null;
                        }
                        return INDEX_HTML;
                    }
                });
    }

    private static boolean isBackendResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            return false;
        }
        String p = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        for (String prefix : BACKEND_RESOURCE_PREFIXES) {
            if (p.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
