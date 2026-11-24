package com.Ticketr.CustomerService.web.dto;

import com.Ticketr.CustomerService.domain.Department;
import com.Ticketr.CustomerService.domain.IssueType;

public record DepartmentResponse(Long id, String name, IssueType handlesIssueType) {
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(d.getId(), d.getName(), d.getHandlesIssueType());
    }
}
