package ma.enset.digitalbanking_spring_angular.dtos;

import lombok.*;
import java.util.Date;

@Data @AllArgsConstructor @NoArgsConstructor @Builder @Setter @Getter
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Date dateOfBirth;
    private String username;
    private String accountNumber;
    private Double balance;
    private String accountType;
}
