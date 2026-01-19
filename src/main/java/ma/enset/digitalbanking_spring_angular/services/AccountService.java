package ma.enset.digitalbanking_spring_angular.services;

import ma.enset.digitalbanking_spring_angular.entities.AppRole;
import ma.enset.digitalbanking_spring_angular.entities.AppUser;
import ma.enset.digitalbanking_spring_angular.entities.Customer;

import java.util.List;

public interface AccountService {
    AppUser addNewUser(String username, String email, String password, String confirmPassword);
    AppRole addNewRole(String roleName);
    void addRoleToUser(String username, String roleName);
    void removeRoleFromUser(String username, String roleName);
    AppUser loadUserByUsername(String username);
    AppUser createUserForCustomer(Customer customer, String password);
    List<AppUser> listUsers();
}
