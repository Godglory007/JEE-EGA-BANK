package ma.enset.digitalbanking_spring_angular.repositories;

import ma.enset.digitalbanking_spring_angular.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    AppRole findByRoleName(String roleName);
}
