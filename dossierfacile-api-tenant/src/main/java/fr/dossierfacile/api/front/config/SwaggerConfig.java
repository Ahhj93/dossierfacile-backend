package fr.dossierfacile.api.front.config;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebFluxRequestHandlerProvider;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("API")
                .securityContexts(List.of(securityContextForClassicAPI()))
                .securitySchemes(List.of(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.dossierfacile.api.front.controller"))
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .apiInfo(apiInfo());
    }

    @Bean
    public Docket apiDossierFacileConnect() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("API DFC")
                .securityContexts(List.of(securityContextForDFCAPI()))
                .securitySchemes(List.of(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.dossierfacile.api.front.dfc.controller"))
                .paths(PathSelectors.ant("/dfc/**"))
                .build()
                .apiInfo(apiDossierFacileConnectInfo());
    }

    @Bean
    public Docket apiPartner() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("API Partner")
                .securityContexts(List.of(securityContextForPartnerAPI()))
                .securitySchemes(List.of(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("fr.dossierfacile.api.front.partner.controller"))
                .paths(PathSelectors.ant("/api-partner/**"))
                .build()
                .apiInfo(apiPartnerInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "DossierFacile REST API",
                "<h3>Private API</h3>\n" +
                        "<div> This API is for internal use </div>",
                "1.0",
                "",
                ApiInfo.DEFAULT_CONTACT,
                "", "", Collections.emptyList());
    }

    private ApiInfo apiDossierFacileConnectInfo() {
        return new ApiInfo(
                "DossierFacile REST API DossierFacileConnect (DFC)",
                "<h3>DFC allows DossierFacile's users to share tenant's data to a partner</h3>\n" +
                        "<div> The link with the partner requires to call the /profile endpoint after the user consent" +
                        "</div>",
                "1.0",
                "",
                ApiInfo.DEFAULT_CONTACT,
                "MIT", "", Collections.emptyList());
    }

    private ApiInfo apiPartnerInfo() {
        return new ApiInfo(
                "DossierFacile REST API Partner",
                "<h3>Description of the api</h3>\n" +
                        "<div>\n" +
                        "    <ul>\n" +
                        "    </ul>\n" +
                        "</div>",
                "1.0",
                "",
                ApiInfo.DEFAULT_CONTACT,
                "", "", Collections.emptyList());
    }

    private ApiInfo apiLinkTenantToPartnerInfo() {
        return new ApiInfo(
                "DossierFacile REST API Link Tenant to Partner",
                "<h3>Description of the api</h3>\n" +
                        "<div>\n" +
                        "    <ul>\n" +
                        "    </ul>\n" +
                        "</div>",
                "1.0",
                "",
                ApiInfo.DEFAULT_CONTACT,
                "", "", Collections.emptyList());
    }

    private ApiKey apiKey() {
        return new ApiKey("Bearer", "Authorization", "header");
    }

    private SecurityContext securityContextForClassicAPI() {
        return SecurityContext.builder().securityReferences(classicAPIAuth()).forPaths(PathSelectors.ant("/api/**")).build();
    }

    private List<SecurityReference> classicAPIAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("SCOPE_dossier", "Access to Classic API");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return List.of(new SecurityReference("Bearer", authorizationScopes));
    }

    private SecurityContext securityContextForDFCAPI() {
        return SecurityContext.builder().securityReferences(dfcAPIAuth()).forPaths(PathSelectors.ant("/dfc/tenant/profile")).build();
    }

    private List<SecurityReference> dfcAPIAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("SCOPE_dfc", "Access to DFC API");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return List.of(new SecurityReference("Bearer", authorizationScopes));
    }

    private SecurityContext securityContextForPartnerAPI() {
        return SecurityContext.builder().securityReferences(partnerAPIAuth()).forPaths(PathSelectors.ant("/api-partner/**")).build();
    }

    private List<SecurityReference> partnerAPIAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("SCOPE_api-partner", "Access to Partner API");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return List.of(new SecurityReference("Bearer", authorizationScopes));
    }

    // hack due to incompatibility between springfox and spring-boot >+2.6
    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            @SuppressWarnings({"NullableProblems", "unchecked"})
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
                    try {
                        Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                        if (null != field) {
                            field.setAccessible(true);
                            List<RequestMappingInfoHandlerMapping> mappings = (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                            mappings.removeIf(e -> null != e.getPatternParser());
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return bean;
            }
        };
    }
}
