package com.Ticketr.CustomerService.repository;

import com.Ticketr.CustomerService.domain.Department;
import com.Ticketr.CustomerService.domain.Role;
import com.Ticketr.CustomerService.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleAndDepartmentAndActiveTrue(Role role, Department department);
}
