package ma.enset.digitalbanking_spring_angular.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.enset.digitalbanking_spring_angular.entities.AppRole;
import ma.enset.digitalbanking_spring_angular.entities.AppUser;
import ma.enset.digitalbanking_spring_angular.entities.Customer;
import ma.enset.digitalbanking_spring_angular.repositories.AppRoleRepository;
import ma.enset.digitalbanking_spring_angular.repositories.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    
    private AppUserRepository appUserRepository;
    private AppRoleRepository appRoleRepository;
    private PasswordEncoder passwordEncoder;
    
    @Override
    public AppUser addNewUser(String username, String email, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new RuntimeException("Passwords do not match");
        }
        if (appUserRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        AppUser appUser = AppUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .active(true)
                .roles(new ArrayList<>())
                .build();
        
        return appUserRepository.save(appUser);
    }
    
    @Override
    public AppRole addNewRole(String roleName) {
        AppRole role = appRoleRepository.findByRoleName(roleName);
        if (role != null) {
            return role;
        }
        return appRoleRepository.save(AppRole.builder().roleName(roleName).build());
    }
    
    @Override
    public void addRoleToUser(String username, String roleName) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = appRoleRepository.findByRoleName(roleName);
        if (appRole == null) {
            throw new RuntimeException("Role not found");
        }
        if (!appUser.getRoles().contains(appRole)) {
            appUser.getRoles().add(appRole);
        }
    }
    
    @Override
    public void removeRoleFromUser(String username, String roleName) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        AppRole appRole = appRoleRepository.findByRoleName(roleName);
        if (appRole != null) {
            appUser.getRoles().remove(appRole);
        }
    }
    
    @Override
    public AppUser loadUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .or(() -> appUserRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Override
    public AppUser createUserForCustomer(Customer customer, String password) {
        // Créer un username à partir du nom (sans espaces, en minuscules)
        String username = customer.getName().toLowerCase().replaceAll("\\s+", "");
        
        // Si le username existe déjà, ajouter l'id du customer
        if (appUserRepository.existsByUsername(username)) {
            username = username + customer.getId();
        }
        
        AppUser appUser = AppUser.builder()
                .username(username)
                .email(customer.getEmail())
                .password(passwordEncoder.encode(password))
                .active(true)
                .roles(new ArrayList<>())
                .customer(customer)
                .build();
        
        // Ajouter le rôle USER par défaut
        AppRole userRole = appRoleRepository.findByRoleName("USER");
        if (userRole != null) {
            appUser.getRoles().add(userRole);
        }
        
        return appUserRepository.save(appUser);
    }
    
    @Override
    public List<AppUser> listUsers() {
        return appUserRepository.findAll();
    }
}
