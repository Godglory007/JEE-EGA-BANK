package ma.enset.digitalbanking_spring_angular;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Digital Banking API",
                version = "1.0",
                description = "API pour la gestion bancaire digitale.\n\nCette API permet de gérer les clients, comptes bancaires, opérations et l'authentification JWT.",
                contact = @Contact(name = "Support GLSI B 2026", email = "support@bank.com", url = "https://bank.com"),
                license = @License(name = "MIT", url = "https://opensource.org/licenses/MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Serveur local de développement")
        },
        tags = {
                @Tag(name = "Authentification", description = "Endpoints d'authentification et de profil utilisateur"),
                @Tag(name = "Comptes", description = "Gestion des comptes bancaires et opérations"),
                @Tag(name = "Clients", description = "Gestion des clients et de leurs informations")
        }
)
//http://localhost:8080/swagger-ui/index.html#
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Entrez votre token JWT (sans 'Bearer ')"
)
public class OpenApiConfig {
}
