package ma.enset.digitalbanking_spring_angular.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbanking_spring_angular.dtos.*;
import ma.enset.digitalbanking_spring_angular.entities.*;
import ma.enset.digitalbanking_spring_angular.entities.enums.AccountStatus;
import ma.enset.digitalbanking_spring_angular.entities.enums.OperationType;
import ma.enset.digitalbanking_spring_angular.exception.BalanceInsiffucientException;
import ma.enset.digitalbanking_spring_angular.exception.BankAccountNotFoundException;
import ma.enset.digitalbanking_spring_angular.exception.CustomerNotFoundException;
import ma.enset.digitalbanking_spring_angular.mappers.BankAccountMapperImpl;
import ma.enset.digitalbanking_spring_angular.repositories.AccountOperationRepository;
import ma.enset.digitalbanking_spring_angular.repositories.AppUserRepository;
import ma.enset.digitalbanking_spring_angular.repositories.BankAccountRepository;
import ma.enset.digitalbanking_spring_angular.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {

    // Code banque fixe pour l'application (5 chiffres)
    private static final String CODE_BANQUE = "00254";
    // Code guichet fixe (5 chiffres)
    private static final String CODE_GUICHET = "01001";

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl bankAccountMapper;
    private AccountService accountService;
    private AppUserRepository appUserRepository;

    @Override
    public Customer saveCustomer(Customer customer) {
        return null;
    }

    @Override
    public CustomerDTO saveCustomerDTO(CustomerDTO customerDTO) {
        log.info("Saving customer");
        Customer customer = bankAccountMapper.toCustomerDTO(customerDTO);
        Customer customerEntity = customerRepository.save(customer);
        
        // Créer automatiquement un compte utilisateur pour ce client
        // Le mot de passe par défaut est "123456" - le client devra le changer
        String defaultPassword = "123456";
        try {
            accountService.createUserForCustomer(customerEntity, defaultPassword);
            log.info("User account created for customer: {}", customerEntity.getEmail());
        } catch (Exception e) {
            log.warn("Could not create user account for customer: {}", e.getMessage());
        }
        
        return bankAccountMapper.fromCustomer(customerEntity);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        Customer customer = bankAccountMapper.toCustomerDTO(customerDTO);
        Customer customerEntity = customerRepository.save(customer);
        return bankAccountMapper.fromCustomer(customerEntity);
    }

    @Override
    public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        
        // Supprimer les comptes bancaires du client (les opérations seront supprimées en cascade)
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        for (BankAccount account : accounts) {
            // Supprimer les opérations du compte
            List<AccountOperation> operations = accountOperationRepository.findByBankAccountId(account.getId());
            accountOperationRepository.deleteAll(operations);
            // Supprimer le compte
            bankAccountRepository.delete(account);
        }
        
        // Supprimer l'utilisateur associé si existe
        AppUser appUser = appUserRepository.findByCustomerId(customerId);
        if (appUser != null) {
            appUserRepository.delete(appUser);
        }
        
        // Supprimer le client
        customerRepository.delete(customer);
    }

    @Override
    public void saveCurrentAccount(double balance, double overdraft, Long customerId) throws CustomerNotFoundException {
        Customer client = customerRepository.findById(customerId).orElse(null);
        if(client == null) {
            throw new CustomerNotFoundException("Customer not found");
        }
        CurrentAccount currentAccount = CurrentAccount.builder()
                .id(generateAccountNumber())
                .balance(balance)
                .customer(client)
                .status(AccountStatus.CREATED)
                .creationDate(new Date())
                .overdraft(overdraft)
                .build();
        bankAccountRepository.save(currentAccount);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentAccountDTO(double balance, double overdraft, Long customerId) throws CustomerNotFoundException {
        Customer client = customerRepository.findById(customerId).orElse(null);
        if(client == null) {
            throw new CustomerNotFoundException("Customer not found");
        }
        CurrentAccount currentAccount = CurrentAccount.builder()
                .id(generateAccountNumber())
                .balance(balance)
                .customer(client)
                .status(AccountStatus.CREATED)
                .creationDate(new Date())
                .overdraft(overdraft)
                .build();
        bankAccountRepository.save(currentAccount);
        return bankAccountMapper.fromCurrentBankAccount(currentAccount);
    }

    @Override
    public void saveSavingAccount(double balance, double rate, Long customerId) throws CustomerNotFoundException {

    }

    @Override
    public SavingBankAccountDTO saveSavingAccountDTO(double balance, double rate, Long customerId) throws CustomerNotFoundException {
        Customer client = customerRepository.findById(customerId).orElse(null);
        if(client == null) {
            throw new CustomerNotFoundException("Customer not found");
        }
        SavingAccount savingAccount = SavingAccount.builder()
                .id(generateAccountNumber())
                .balance(balance)
                .customer(client)
                .status(AccountStatus.CREATED)
                .creationDate(new Date())
                .rate(rate)
                .build();
        bankAccountRepository.save(savingAccount);
        return bankAccountMapper.fromSavingBankAccount(savingAccount);
    }

    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream().map(c->bankAccountMapper.fromCustomer(c)).collect(Collectors.toList());
        return customerDTOS;
    }

    @Override
    public List<BankAccountDTO> listBankAccounts() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof CurrentAccount) {
                return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            } else if(bankAccount instanceof SavingAccount) {
                return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            }
            return null;
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Account not found"));
        if(bankAccount instanceof CurrentAccount) {
//            return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        } else if(bankAccount instanceof SavingAccount) {
            return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        }
        return null;
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceInsiffucientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Account not found"));
        if(bankAccount.getBalance() < amount) {
            throw new BalanceInsiffucientException("Insufficient balance");
        }
        accountOperationRepository.save(AccountOperation.builder()
                .amount(amount)
                .bankAccount(bankAccount)
                .description(description)
                .operationDate(new Date())
                .operationType(OperationType.DEBIT)
                .build());
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(() -> new BankAccountNotFoundException("Account not found"));
        accountOperationRepository.save(AccountOperation.builder()
                .amount(amount)
                .bankAccount(bankAccount)
                .description(description)
                .operationDate(new Date())
                .operationType(OperationType.CREDIT)
                .build());
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String fromAccountId, String toAccountId, double amount) throws BankAccountNotFoundException, BalanceInsiffucientException {
        debit(fromAccountId,amount,"Transfer to "+toAccountId);
        credit(toAccountId,amount,"Transfer from "+fromAccountId);
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return bankAccountMapper.fromCustomer(customer);
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(accountOperation -> bankAccountMapper.fromAccountOperation(accountOperation)).collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String id, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(id).orElse(null);
        if (bankAccount == null) {
            throw new BankAccountNotFoundException("Account not found");
        }

        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(id, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream()
                .map(bankAccountMapper::fromAccountOperation)
                .collect(Collectors.toList());

        accountHistoryDTO.setAccountOperations(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setAccountType(bankAccount.getClass().getSimpleName());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        accountHistoryDTO.setPageSize(size);
        return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        return customerRepository.searchCustomer(keyword).stream().map(bankAccountMapper::fromCustomer).collect(Collectors.toList());
    }
    
    @Override
    public List<BankAccountDTO> getCustomerAccounts(Long customerId) {
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        return accounts.stream().map(account -> {
            if (account instanceof CurrentAccount) {
                return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) account);
            } else {
                return bankAccountMapper.fromSavingBankAccount((SavingAccount) account);
            }
        }).collect(Collectors.toList());
    }
    
    @Override
    public String generateAccountNumber() {
        Random random = new Random();
        
        // Générer un numéro de compte unique (11 chiffres)
        StringBuilder numeroCompte = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            numeroCompte.append(random.nextInt(10));
        }
        
        // Calculer la clé RIB (2 chiffres)
        // Formule : 97 - ((89 × CodeBanque + 15 × CodeGuichet + 3 × NuméroCompte) mod 97)
        long codeBanque = Long.parseLong(CODE_BANQUE);
        long codeGuichet = Long.parseLong(CODE_GUICHET);
        long numCompte = Long.parseLong(numeroCompte.toString());
        
        long cleRib = 97 - ((89 * codeBanque + 15 * codeGuichet + 3 * numCompte) % 97);
        
        // Formater la clé RIB sur 2 chiffres
        String cleRibStr = String.format("%02d", cleRib);
        
        // Retourner le numéro complet au format : CodeBanque-CodeGuichet-NuméroCompte-CléRIB
        return CODE_BANQUE + "-" + CODE_GUICHET + "-" + numeroCompte + "-" + cleRibStr;
    }
    
    @Override
    public CustomerFullDTO createCustomerWithAccount(CreateCustomerRequest request) {
        log.info("Creating customer with account: {}", request.getName());
        
        // 1. Créer le client
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .dateOfBirth(request.getDateOfBirth())
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        
        // 2. Créer le compte utilisateur
        String password = request.getPassword() != null ? request.getPassword() : "123456";
        AppUser appUser = accountService.createUserForCustomer(savedCustomer, password);
        
        // 3. Créer le compte bancaire avec numéro automatique
        String accountNumber = generateAccountNumber();
        double initialBalance = request.getInitialBalance() != null ? request.getInitialBalance() : 0.0;
        
        BankAccount bankAccount;
        if ("SAVING".equalsIgnoreCase(request.getAccountType())) {
            double rate = request.getInterestRate() != null ? request.getInterestRate() : 5.5;
            SavingAccount savingAccount = SavingAccount.builder()
                    .id(accountNumber)
                    .balance(initialBalance)
                    .customer(savedCustomer)
                    .status(AccountStatus.ACTIVE)
                    .creationDate(new Date())
                    .rate(rate)
                    .build();
            bankAccount = bankAccountRepository.save(savingAccount);
        } else {
            double overdraft = request.getOverdraft() != null ? request.getOverdraft() : 500.0;
            CurrentAccount currentAccount = CurrentAccount.builder()
                    .id(accountNumber)
                    .balance(initialBalance)
                    .customer(savedCustomer)
                    .status(AccountStatus.ACTIVE)
                    .creationDate(new Date())
                    .overdraft(overdraft)
                    .build();
            bankAccount = bankAccountRepository.save(currentAccount);
        }
        
        log.info("Created account {} for customer {}", accountNumber, savedCustomer.getName());
        
        // Charger les comptes du client explicitement
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(savedCustomer.getId());
        savedCustomer.setBankAccounts(accounts);
        
        return bankAccountMapper.toCustomerFullDTO(savedCustomer, appUser);
    }
    
    @Override
    public CustomerFullDTO getCustomerFull(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        
        // Charger les comptes explicitement
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customerId);
        customer.setBankAccounts(accounts);
        
        AppUser appUser = appUserRepository.findByCustomer(customer).orElse(null);
        return bankAccountMapper.toCustomerFullDTO(customer, appUser);
    }
    
    @Override
    public List<BankAccountDTO> getMyAccounts(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .or(() -> appUserRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (appUser.getCustomer() == null) {
            return List.of();
        }
        
        return getCustomerAccounts(appUser.getCustomer().getId());
    }
    
    @Override
    public List<AccountOperationDTO> getMyOperations(String username, int page, int size) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .or(() -> appUserRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (appUser.getCustomer() == null) {
            return List.of();
        }
        
        // Récupérer tous les comptes du client
        List<BankAccount> accounts = bankAccountRepository.findByCustomerId(appUser.getCustomer().getId());
        
        // Récupérer toutes les opérations de tous les comptes
        List<AccountOperationDTO> allOperations = new java.util.ArrayList<>();
        for (BankAccount account : accounts) {
            List<AccountOperation> operations = accountOperationRepository.findByBankAccountId(account.getId());
            allOperations.addAll(operations.stream()
                    .map(bankAccountMapper::fromAccountOperation)
                    .peek(op -> op.setAccountId(account.getId()))
                    .collect(Collectors.toList()));
        }
        
        // Trier par date décroissante et paginer
        allOperations.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        int start = page * size;
        int end = Math.min(start + size, allOperations.size());
        
        if (start >= allOperations.size()) {
            return List.of();
        }
        
        return allOperations.subList(start, end);
    }

}
