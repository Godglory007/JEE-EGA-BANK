package ma.enset.digitalbanking_spring_angular;
import ma.enset.digitalbanking_spring_angular.dtos.BankAccountDTO;
import ma.enset.digitalbanking_spring_angular.dtos.CurrentBankAccountDTO;
import ma.enset.digitalbanking_spring_angular.dtos.CustomerDTO;
import ma.enset.digitalbanking_spring_angular.dtos.SavingBankAccountDTO;
import ma.enset.digitalbanking_spring_angular.entities.*;
import ma.enset.digitalbanking_spring_angular.entities.enums.AccountStatus;
import ma.enset.digitalbanking_spring_angular.entities.enums.OperationType;
import ma.enset.digitalbanking_spring_angular.exception.BalanceInsiffucientException;
import ma.enset.digitalbanking_spring_angular.exception.BankAccountNotFoundException;
import ma.enset.digitalbanking_spring_angular.exception.CustomerNotFoundException;
import ma.enset.digitalbanking_spring_angular.repositories.AccountOperationRepository;
import ma.enset.digitalbanking_spring_angular.repositories.AppRoleRepository;
import ma.enset.digitalbanking_spring_angular.repositories.BankAccountRepository;
import ma.enset.digitalbanking_spring_angular.repositories.CustomerRepository;
import ma.enset.digitalbanking_spring_angular.services.AccountService;
import ma.enset.digitalbanking_spring_angular.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@SpringBootApplication
public class DigitalBankingSpringAngularApplication {

    // Code banque fixe pour l'application (5 chiffres)
    private static final String CODE_BANQUE = "00254";
    // Code guichet fixe (5 chiffres)
    private static final String CODE_GUICHET = "01001";
    
    /**
     * Génère un numéro de compte au format RIB
     */
    private static String generateAccountNumber() {
        Random random = new Random();
        StringBuilder numeroCompte = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            numeroCompte.append(random.nextInt(10));
        }
        long codeBanque = Long.parseLong(CODE_BANQUE);
        long codeGuichet = Long.parseLong(CODE_GUICHET);
        long numCompte = Long.parseLong(numeroCompte.toString());
        long cleRib = 97 - ((89 * codeBanque + 15 * codeGuichet + 3 * numCompte) % 97);
        String cleRibStr = String.format("%02d", cleRib);
        return CODE_BANQUE + "-" + CODE_GUICHET + "-" + numeroCompte + "-" + cleRibStr;
    }

    public static void main(String[] args) {
        SpringApplication.run(DigitalBankingSpringAngularApplication.class, args);
    }

    @Bean
    CommandLineRunner initRolesAndAdmin(AccountService accountService, AppRoleRepository appRoleRepository) {
        return args -> {
            // Créer les rôles s'ils n'existent pas
            if (appRoleRepository.findByRoleName("ADMIN") == null) {
                accountService.addNewRole("ADMIN");
            }
            if (appRoleRepository.findByRoleName("MANAGER") == null) {
                accountService.addNewRole("MANAGER");
            }
            if (appRoleRepository.findByRoleName("USER") == null) {
                accountService.addNewRole("USER");
            }
            
            
            try {
                accountService.loadUserByUsername("admin");
            } catch (Exception e) {
                
                accountService.addNewUser("admin", "admin@bank.com", "123", "123");
                accountService.addRoleToUser("admin", "ADMIN");
                accountService.addRoleToUser("admin", "MANAGER");
                accountService.addRoleToUser("admin", "USER");
            }
            
            try {
                accountService.loadUserByUsername("glory");
            } catch (Exception e) {
                accountService.addNewUser("glory", "glory@bank.com", "glory", "glory");
            }
            // Toujours ajouter les rôles à glory, même s'il existe déjà
            accountService.addRoleToUser("glory", "ADMIN");
            accountService.addRoleToUser("glory", "MANAGER");
            accountService.addRoleToUser("glory", "USER");
        };
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountService bankAccountService, CustomerRepository customerRepository) {
        return args -> {
            // Vérifier si les données existent déjà
            if (customerRepository.count() > 0) {
                System.out.println("Données déjà initialisées. Skip...");
                return;
            }
            
            Stream.of("Zaid", "Hassan", "Yassine").forEach(name ->
                    bankAccountService.saveCustomerDTO(CustomerDTO.builder()
                            .name(name)
                            .email(name+"@gmail.com")
                            .build()
                    )
            );
            // creer les comptes bancaires
            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentAccountDTO(Math.random()*9000, 500, customer.getId());
                    bankAccountService.saveSavingAccountDTO(Math.random()*9000, 5.5, customer.getId());

                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });

            List<BankAccountDTO> bankAccounts = bankAccountService.listBankAccounts();
            for (BankAccountDTO bankAccount : bankAccounts) {
                for (int i = 0; i < 5; i++) {
                    String accountId;
                    if(bankAccount instanceof CurrentBankAccountDTO) {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    } else {
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, Math.random()*6000, "Initial credit");
                    bankAccountService.debit(accountId, Math.random()*600, "Initial debit");
                }
            }
        };
    }

//    @Bean
    CommandLineRunner start(CustomerRepository customerRepository, AccountOperationRepository accountOperationRepository, BankAccountRepository bankAccountRepository) {
        return args -> {
            // Creer un client
            Stream.of("Zaid", "Hassan", "Yassine").forEach(name ->
                    customerRepository.save(Customer.builder()
                    .name(name)
                    .email(name+"@gmail.com")
                    .build()
            ));

            //Creer compte bancaire
            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = CurrentAccount.builder()
                        .id(generateAccountNumber())
                        .customer(customer)
                        .balance(Math.random()*9000)
                        .creationDate(new Date())
                        .status(AccountStatus.CREATED)
                        .overdraft(500)
                        .build();
                bankAccountRepository.save(currentAccount);

                bankAccountRepository.save(SavingAccount.builder()
                        .id(generateAccountNumber())
                        .customer(customer)
                        .balance(Math.random()*9000)
                        .creationDate(new Date())
                        .status(AccountStatus.CREATED)
                        .rate(5.5)
                        .build()
                );
            });

            // pour les operations
            bankAccountRepository.findAll().forEach(bankAccount -> {
                for (int i = 0; i < 5; i++) {
                    accountOperationRepository.save(AccountOperation.builder()
                            .amount(Math.random()*6000)
                            .operationDate(new Date())
                            .operationType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT)
                            .bankAccount(bankAccount)
                            .build()
                    );
                }
            });

        };
    }
}