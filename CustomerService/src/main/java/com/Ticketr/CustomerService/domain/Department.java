package com.Ticketr.CustomerService.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** The issue type this department is responsible for. One department per issue type in this starter set. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private IssueType handlesIssueType;

    public Department(String name, IssueType handlesIssueType) {
        this.name = name;
        this.handlesIssueType = handlesIssueType;
    }
}
