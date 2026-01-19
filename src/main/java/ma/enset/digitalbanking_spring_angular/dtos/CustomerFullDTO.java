package ma.enset.digitalbanking_spring_angular.dtos;

import lombok.*;
import java.util.Date;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CustomerFullDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Date dateOfBirth;
    private String username;
    private List<AccountInfoDTO> accounts;
}
