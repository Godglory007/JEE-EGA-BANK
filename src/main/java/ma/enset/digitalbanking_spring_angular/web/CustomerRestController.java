package ma.enset.digitalbanking_spring_angular.web;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbanking_spring_angular.dtos.*;
import ma.enset.digitalbanking_spring_angular.exception.CustomerNotFoundException;
import ma.enset.digitalbanking_spring_angular.services.BankAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
@SecurityRequirement(name = "bearerAuth")
public class CustomerRestController {
    private BankAccountService bankAccountService;

    @Operation(
        summary = "Lister tous les clients",
        description = "Retourne la liste de tous les clients (admin uniquement).",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des clients", content = @Content(schema = @Schema(implementation = CustomerDTO.class)))
        }
    )
    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<CustomerDTO> customers() {
        return bankAccountService.listCustomers();
    }


    @Operation(
        summary = "Obtenir un client par ID",
        description = "Retourne les informations d'un client spécifique (admin uniquement).",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Client trouvé", content = @Content(schema = @Schema(implementation = CustomerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
        }
    )
    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long CustomerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(CustomerId);
    }

    @Operation(
        summary = "Créer un nouveau client",
        description = "Crée un nouveau client (admin uniquement).",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Client créé", content = @Content(schema = @Schema(implementation = CustomerDTO.class)))
        }
    )
    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customer) {
        return bankAccountService.saveCustomerDTO(customer);
    }

    @Operation(
        summary = "Mettre à jour un client",
        description = "Met à jour les informations d'un client (admin uniquement).",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Client mis à jour", content = @Content(schema = @Schema(implementation = CustomerDTO.class)))
        }
    )
    @PutMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO updateCustomer(@PathVariable Long id,@RequestBody CustomerDTO customerDTO) {
        customerDTO.setId(id);
        return bankAccountService.updateCustomer(customerDTO);
    }

    @Operation(
        summary = "Supprimer un client",
        description = "Supprime un client par ID (admin uniquement).",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Client supprimé"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
        }
    )
    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public void deleteCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        bankAccountService.deleteCustomer(id);
    }

    @Operation(
        summary = "Rechercher des clients par mot-clé",
        description = "Recherche les clients dont le nom ou l'email contient le mot-clé.",
        tags = {"Clients"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des clients trouvés", content = @Content(schema = @Schema(implementation = CustomerDTO.class)))
        }
    )
    @GetMapping("/customers/search")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword", defaultValue = "") String keyword) {
        return bankAccountService.searchCustomers("%" + keyword + "%");
    }

    // ============ NOUVELLES FONCTIONNALITÉS ============

    /**
     * Créer un client complet avec compte utilisateur et compte bancaire
     * Accessible uniquement par les administrateurs
     */
    @PostMapping("/customers/full")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerFullDTO createCustomerWithAccount(@RequestBody CreateCustomerRequest request) {
        log.info("Creating customer with full account: {}", request.getName());
        return bankAccountService.createCustomerWithAccount(request);
    }

    /**
     * Obtenir les détails complets d'un client avec ses comptes
     */
    @GetMapping("/customers/{id}/full")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerFullDTO getCustomerFull(@PathVariable Long id) throws CustomerNotFoundException {
        return bankAccountService.getCustomerFull(id);
    }

    /**
     * Endpoint pour qu'un client connecté voit ses opérations
     */
    @GetMapping("/my-operations")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_USER')")
    public List<AccountOperationDTO> getMyOperations(Authentication authentication,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        String username = authentication.getName();
        log.info("User {} requesting their operations", username);
        return bankAccountService.getMyOperations(username, page, size);
    }
}
