package com.Ticketr.CustomerService.web.dto;

import com.Ticketr.CustomerService.domain.IssueType;
import com.Ticketr.CustomerService.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank(message = "Subject is required") String subject,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Issue type is required") IssueType issueType,
        @NotNull(message = "Priority is required") Priority priority
) {
}
