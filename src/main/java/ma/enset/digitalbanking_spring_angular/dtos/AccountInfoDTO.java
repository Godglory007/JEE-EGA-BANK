package ma.enset.digitalbanking_spring_angular.dtos;

import lombok.*;
import java.util.Date;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class AccountInfoDTO {
    private String accountNumber;
    private String accountType;
    private Double balance;
    private String status;
    private Date creationDate;
}
