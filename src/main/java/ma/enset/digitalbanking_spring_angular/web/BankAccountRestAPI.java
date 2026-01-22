package ma.enset.digitalbanking_spring_angular.web;
import ma.enset.digitalbanking_spring_angular.dtos.AccountHistoryDTO;
import ma.enset.digitalbanking_spring_angular.dtos.AccountOperationDTO;
import ma.enset.digitalbanking_spring_angular.dtos.BankAccountDTO;
import ma.enset.digitalbanking_spring_angular.dtos.SavingBankAccountDTO;
import ma.enset.digitalbanking_spring_angular.dtos.TransferRequestDTO;
import ma.enset.digitalbanking_spring_angular.entities.AppUser;
import ma.enset.digitalbanking_spring_angular.exception.BalanceInsiffucientException;
import ma.enset.digitalbanking_spring_angular.exception.BankAccountNotFoundException;
import ma.enset.digitalbanking_spring_angular.repositories.AppUserRepository;
import ma.enset.digitalbanking_spring_angular.services.BankAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountService bankAccountService;
    private AppUserRepository appUserRepository;

    public BankAccountRestAPI(BankAccountService bankAccountService, AppUserRepository appUserRepository) {
        this.bankAccountService = bankAccountService;
        this.appUserRepository = appUserRepository;
    }

    @Operation(
        summary = "Obtenir un compte bancaire par ID",
        description = "Retourne les informations d'un compte bancaire spécifique.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Compte trouvé", content = @Content(schema = @Schema(implementation = BankAccountDTO.class))),
            @ApiResponse(responseCode = "404", description = "Compte non trouvé")
        }
    )
    @GetMapping("/accounts/{id}")
    public BankAccountDTO getBankAccount(@PathVariable String id) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(id);
    }

    @Operation(
        summary = "Lister tous les comptes bancaires",
        description = "Retourne la liste de tous les comptes bancaires (admin uniquement).",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des comptes", content = @Content(schema = @Schema(implementation = BankAccountDTO.class)))
        }
    )
    @GetMapping("/accounts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<BankAccountDTO> listBankAccounts() {
        return bankAccountService.listBankAccounts();
    }
    
    @Operation(
        summary = "Lister les comptes de l'utilisateur connecté",
        description = "Retourne la liste des comptes bancaires de l'utilisateur connecté.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des comptes de l'utilisateur", content = @Content(schema = @Schema(implementation = BankAccountDTO.class)))
        }
    )
    @GetMapping("/my-accounts")
    public List<BankAccountDTO> getMyAccounts(Authentication authentication) {
        String username = authentication.getName();
        AppUser appUser = appUserRepository.findByUsername(username)
                .or(() -> appUserRepository.findByEmail(username))
                .orElse(null);
        if (appUser != null && appUser.getCustomer() != null) {
            return bankAccountService.getCustomerAccounts(appUser.getCustomer().getId());
        }
        return new ArrayList<>();
    }
    @Operation(
        summary = "Historique des opérations d'un compte",
        description = "Retourne la liste des opérations d'un compte bancaire.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des opérations", content = @Content(schema = @Schema(implementation = AccountOperationDTO.class)))
        }
    )
    @GetMapping("/accounts/{id}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String id) {
        return bankAccountService.accountHistory(id);
    }

    @Operation(
        summary = "Historique paginé d'un compte",
        description = "Retourne l'historique paginé des opérations d'un compte.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Historique paginé", content = @Content(schema = @Schema(implementation = AccountHistoryDTO.class)))
        }
    )
    @GetMapping("/accounts/{id}/pageOperations")
    public AccountHistoryDTO getAccountHistory(@PathVariable String id, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name="size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(id, page, size);
    }

    //debit trasfere credit
    @Operation(
        summary = "Créditer un compte bancaire",
        description = "Ajoute un montant au solde d'un compte.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Crédit effectué")
        }
    )
    @PostMapping("/accounts/credit/{id}")
    public void credit(@PathVariable String id, @RequestParam double amount, @RequestParam String desc) throws BankAccountNotFoundException {
        bankAccountService.credit(id, amount, desc);
    }

    @Operation(
        summary = "Débiter un compte bancaire",
        description = "Retire un montant du solde d'un compte.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Débit effectué")
        }
    )
    @PostMapping("/accounts/debit/{id}")
    public ResponseEntity<String> debit(@PathVariable String id, @RequestParam double amount, @RequestParam String desc) throws BankAccountNotFoundException, BalanceInsiffucientException {
        bankAccountService.debit(id, amount, desc);
        return ResponseEntity.ok("Débité avec succès");
    }

    @Operation(
        summary = "Transférer entre deux comptes",
        description = "Transfère un montant d'un compte source vers un compte destination.",
        tags = {"Comptes"},
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Transfert effectué")
        }
    )
    @PostMapping("/accounts/transfer")
    public void transfer(@RequestBody TransferRequestDTO request) throws BankAccountNotFoundException, BalanceInsiffucientException {
        bankAccountService.transfer(request.getAccountSource(), request.getAccountDestination(), request.getAmount());
    }

}
