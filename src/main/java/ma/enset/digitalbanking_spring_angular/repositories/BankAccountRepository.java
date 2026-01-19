package ma.enset.digitalbanking_spring_angular.repositories;

import ma.enset.digitalbanking_spring_angular.entities.BankAccount;
import ma.enset.digitalbanking_spring_angular.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String>{
    List<BankAccount> findByCustomerId(Long customerId);
}
