package ma.enset.digitalbanking_spring_angular.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbanking_spring_angular.entities.AccountOperation;
import ma.enset.digitalbanking_spring_angular.entities.BankAccount;
import ma.enset.digitalbanking_spring_angular.entities.CurrentAccount;
import ma.enset.digitalbanking_spring_angular.entities.SavingAccount;
import ma.enset.digitalbanking_spring_angular.repositories.AccountOperationRepository;
import ma.enset.digitalbanking_spring_angular.repositories.BankAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service de migration pour convertir les anciens numéros de compte (UUID)
 * vers le nouveau format RIB : CodeBanque-CodeGuichet-NuméroCompte-CléRIB
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // S'exécute en premier
public class AccountMigrationService implements CommandLineRunner {

    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    // Code banque fixe pour l'application (5 chiffres)
    private static final String CODE_BANQUE = "00254";
    // Code guichet fixe (5 chiffres)
    private static final String CODE_GUICHET = "01001";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== Démarrage de la migration des numéros de compte ===");
        
        List<BankAccount> accounts = bankAccountRepository.findAll();
        int migratedCount = 0;
        
        for (BankAccount account : accounts) {
            String oldId = account.getId();
            
            // Vérifier si le compte a déjà le nouveau format (contient des tirets au bon endroit)
            if (isNewFormat(oldId)) {
                log.debug("Compte {} déjà au nouveau format, ignoré", oldId);
                continue;
            }
            
            // Générer le nouveau numéro de compte
            String newId = generateAccountNumber();
            
            log.info("Migration du compte {} -> {}", oldId, newId);
            
            // Mettre à jour les opérations liées à ce compte
            List<AccountOperation> operations = accountOperationRepository.findByBankAccountId(oldId);
            
            // Supprimer l'ancien compte (avec ses opérations)
            accountOperationRepository.deleteAll(operations);
            bankAccountRepository.delete(account);
            bankAccountRepository.flush();
            
            // Créer le nouveau compte avec le nouvel ID
            account.setId(newId);
            BankAccount savedAccount = bankAccountRepository.save(account);
            
            // Recréer les opérations avec le nouveau compte
            for (AccountOperation op : operations) {
                op.setId(null); // Reset ID pour générer un nouveau
                op.setBankAccount(savedAccount);
                accountOperationRepository.save(op);
            }
            
            migratedCount++;
        }
        
        if (migratedCount > 0) {
            log.info("=== Migration terminée : {} compte(s) migré(s) ===", migratedCount);
        } else {
            log.info("=== Aucun compte à migrer ===");
        }
    }

    /**
     * Vérifie si l'ID est déjà au nouveau format RIB
     * Format attendu : 00254-01001-XXXXXXXXXXX-XX
     */
    private boolean isNewFormat(String id) {
        if (id == null) return false;
        // Le nouveau format a exactement 4 parties séparées par des tirets
        // et commence par le code banque
        String[] parts = id.split("-");
        return parts.length == 4 
                && parts[0].equals(CODE_BANQUE) 
                && parts[1].length() == 5 
                && parts[2].length() == 11 
                && parts[3].length() == 2;
    }

    /**
     * Génère un numéro de compte au format RIB : CodeBanque-CodeGuichet-NuméroCompte-CléRIB
     */
    private String generateAccountNumber() {
        Random random = new Random();
        
        // Générer un numéro de compte unique (11 chiffres)
        StringBuilder numeroCompte = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            numeroCompte.append(random.nextInt(10));
        }
        
        // Calculer la clé RIB (2 chiffres)
        long codeBanque = Long.parseLong(CODE_BANQUE);
        long codeGuichet = Long.parseLong(CODE_GUICHET);
        long numCompte = Long.parseLong(numeroCompte.toString());
        
        long cleRib = 97 - ((89 * codeBanque + 15 * codeGuichet + 3 * numCompte) % 97);
        String cleRibStr = String.format("%02d", cleRib);
        
        return CODE_BANQUE + "-" + CODE_GUICHET + "-" + numeroCompte + "-" + cleRibStr;
    }
}
