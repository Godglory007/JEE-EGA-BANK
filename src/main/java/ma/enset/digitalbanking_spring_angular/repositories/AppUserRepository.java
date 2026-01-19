package ma.enset.digitalbanking_spring_angular.repositories;

import ma.enset.digitalbanking_spring_angular.entities.AppUser;
import ma.enset.digitalbanking_spring_angular.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<AppUser> findByCustomer(Customer customer);
    AppUser findByCustomerId(Long customerId);
}
