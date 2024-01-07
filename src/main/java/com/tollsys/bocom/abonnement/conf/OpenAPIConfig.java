//package com.tollsys.bocom.abonnement.conf;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.info.Contact;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.License;
//import io.swagger.v3.oas.models.servers.Server;
//
//@Configuration
//public class OpenAPIConfig {
//
//    @Value("${app.openapi.dev-url}")
//    private String devUrl;
//
//    @Value("${app.openapi.prod-url}")
//    private String prodUrl;
//
//    @Bean
//    public OpenAPI myOpenAPI() {
//        Server devServer = new Server();
//        devServer.setUrl(devUrl);
//        devServer.setDescription("Server URL in Development environment");
//
//        Server prodServer = new Server();
//        prodServer.setUrl(prodUrl);
//        prodServer.setDescription("Server URL in Production environment");
//
//        Contact contact = new Contact();
//        contact.setEmail("francis.marfella@tollsys.fr");
//        contact.setName("Francis MARFELLA");
//        contact.setUrl("https://tollsys.fr");
//
//        License mitLicense = new License()
//                .name("TollSYS License")
//                .url("https://tollsys.fr/mentions-legales/");
//
//        Info info = new Info()
//                .title("Abonnement API")
//                .version("1.0")
//                .contact(contact)
//                .description("This API exposes endpoints to Abonnements.")
//                .termsOfService("http://your-terms-of-service-url.com")
//                .license(mitLicense);
//
//        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
//    }
//}
