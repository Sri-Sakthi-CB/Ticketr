package com.Ticketr.CustomerService.service;

import com.Ticketr.CustomerService.domain.Department;
import com.Ticketr.CustomerService.domain.IssueType;
import com.Ticketr.CustomerService.domain.Role;
import com.Ticketr.CustomerService.domain.TicketStatus;
import com.Ticketr.CustomerService.domain.User;
import com.Ticketr.CustomerService.exception.BadRequestException;
import com.Ticketr.CustomerService.repository.DepartmentRepository;
import com.Ticketr.CustomerService.repository.TicketRepository;
import com.Ticketr.CustomerService.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Routes a new ticket to the right department (by issue type) and picks the agent to assign it to.
 * Priority does not change which department gets the ticket — it changes the SLA due date and,
 * within the department, tickets go to whichever active agent currently has the fewest open tickets,
 * so high-priority work lands with agents who have capacity rather than piling onto one person.
 */
@Service
public class AssignmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public AssignmentService(DepartmentRepository departmentRepository, UserRepository userRepository, TicketRepository ticketRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    public Department resolveDepartment(IssueType issueType) {
        return departmentRepository.findByHandlesIssueType(issueType)
                .orElseThrow(() -> new BadRequestException("No department is configured to handle issue type " + issueType));
    }

    public User pickAgent(Department department) {
        List<User> agents = userRepository.findByRoleAndDepartmentAndActiveTrue(Role.AGENT, department);
        if (agents.isEmpty()) {
            throw new BadRequestException("No active agents available in department " + department.getName());
        }
        return agents.stream()
                .min(Comparator.comparingLong(agent -> ticketRepository.countByAssignedAgentAndStatusNot(agent, TicketStatus.CLOSED)))
                .orElseThrow();
    }
}
