package com.Ticketr.CustomerService.web;

import com.Ticketr.CustomerService.repository.DepartmentRepository;
import com.Ticketr.CustomerService.web.dto.DepartmentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<DepartmentResponse> list() {
        return departmentRepository.findAll().stream().map(DepartmentResponse::from).toList();
    }
}
