package com.Ticketr.CustomerService.web.dto;

import com.Ticketr.CustomerService.domain.*;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String subject,
        String description,
        IssueType issueType,
        Priority priority,
        TicketStatus status,
        String customerName,
        String departmentName,
        Long assignedAgentId,
        String assignedAgentName,
        String resolutionNote,
        Instant createdAt,
        Instant updatedAt,
        Instant dueAt,
        Instant resolvedAt,
        Instant closedAt,
        // Server-computed permission flags: the frontend renders buttons from these rather than
        // re-implementing the authorization rules itself.
        boolean canStart,
        boolean canResolve,
        boolean canClose,
        boolean canReopen
) {
    public static TicketResponse from(Ticket t, User viewer) {
        boolean isAssignedAgent = t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(viewer.getId());
        boolean isOwner = t.getCustomer().getId().equals(viewer.getId());

        boolean canStart = isAssignedAgent && t.getStatus() == TicketStatus.ASSIGNED;
        boolean canResolve = isAssignedAgent
                && (t.getStatus() == TicketStatus.IN_PROGRESS || t.getStatus() == TicketStatus.REOPENED);
        // The checkpoint: CLOSED is reachable only from RESOLVED, and only by the assigned agent.
        boolean canClose = isAssignedAgent && t.getStatus() == TicketStatus.RESOLVED;
        boolean canReopen = isOwner && t.getStatus() == TicketStatus.RESOLVED;

        return new TicketResponse(
                t.getId(),
                t.getSubject(),
                t.getDescription(),
                t.getIssueType(),
                t.getPriority(),
                t.getStatus(),
                t.getCustomer().getName(),
                t.getDepartment() == null ? null : t.getDepartment().getName(),
                t.getAssignedAgent() == null ? null : t.getAssignedAgent().getId(),
                t.getAssignedAgent() == null ? null : t.getAssignedAgent().getName(),
                t.getResolutionNote(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getDueAt(),
                t.getResolvedAt(),
                t.getClosedAt(),
                canStart,
                canResolve,
                canClose,
                canReopen
        );
    }
}
