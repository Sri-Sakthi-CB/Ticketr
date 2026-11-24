package com.Ticketr.CustomerService.repository;

import com.Ticketr.CustomerService.domain.Department;
import com.Ticketr.CustomerService.domain.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByHandlesIssueType(IssueType issueType);
}
