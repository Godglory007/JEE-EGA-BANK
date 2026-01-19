package ma.enset.digitalbanking_spring_angular.dtos;

import lombok.*;
import java.util.Date;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CreateCustomerRequest {
    private String name;
    private String email;
    private String phone;
    private Date dateOfBirth;
    private String password;  // Mot de passe pour le compte utilisateur
    private String accountType;  // "CURRENT" ou "SAVING"
    private Double initialBalance;  // Solde initial du compte
    private Double overdraft;  // Pour compte courant
    private Double interestRate;  // Pour compte épargne
}
