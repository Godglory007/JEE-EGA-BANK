package ma.enset.digitalbanking_spring_angular.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbanking_spring_angular.dtos.*;
import ma.enset.digitalbanking_spring_angular.exception.CustomerNotFoundException;
import ma.enset.digitalbanking_spring_angular.services.BankAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomerRestController {
    private BankAccountService bankAccountService;

    @GetMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public List<CustomerDTO> customers() {
        return bankAccountService.listCustomers();
    }


    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long CustomerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(CustomerId);
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customer) {
        return bankAccountService.saveCustomerDTO(customer);
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public CustomerDTO updateCustomer(@PathVariable Long id,@RequestBody CustomerDTO customerDTO) {
        customerDTO.setId(id);
        return bankAccountService.updateCustomer(customerDTO);
    }

    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    public void deleteCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        bankAccountService.deleteCustomer(id);
    }

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
