package com.Ticketr.CustomerService.seed;

import com.Ticketr.CustomerService.domain.Department;
import com.Ticketr.CustomerService.domain.IssueType;
import com.Ticketr.CustomerService.domain.Role;
import com.Ticketr.CustomerService.domain.User;
import com.Ticketr.CustomerService.repository.DepartmentRepository;
import com.Ticketr.CustomerService.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the fixed starter department set, the issue-type -> department routing rules, and demo
 * accounts for every role so the app is usable immediately after a fresh start. Runs only if the
 * departments table is empty, so it's safe on every restart.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(DepartmentRepository departmentRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() > 0) {
            return;
        }

        Department billing = departmentRepository.save(new Department("Billing", IssueType.BILLING));
        Department technical = departmentRepository.save(new Department("Technical Support", IssueType.TECHNICAL));
        Department account = departmentRepository.save(new Department("Account & Access", IssueType.ACCOUNT));
        Department general = departmentRepository.save(new Department("General Inquiries", IssueType.GENERAL));

        String demoPassword = passwordEncoder.encode("password123");

        userRepository.save(new User("Site Admin", "admin@demo.com", demoPassword, Role.ADMIN, null));

        userRepository.save(new User("Bailey Ng", "agent.billing@demo.com", demoPassword, Role.AGENT, billing));
        userRepository.save(new User("Priya Shah", "agent.technical1@demo.com", demoPassword, Role.AGENT, technical));
        userRepository.save(new User("Marco Diaz", "agent.technical2@demo.com", demoPassword, Role.AGENT, technical));
        userRepository.save(new User("Wren Foster", "agent.account@demo.com", demoPassword, Role.AGENT, account));
        userRepository.save(new User("Sam Ito", "agent.general@demo.com", demoPassword, Role.AGENT, general));

        userRepository.save(new User("Demo Customer", "customer@demo.com", demoPassword, Role.CUSTOMER, null));
    }
}
