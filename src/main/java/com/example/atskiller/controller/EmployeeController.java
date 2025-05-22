//package com.example.atskiller.controller;
//
//import com.example.atskiller.model.Employee;
//import com.example.atskiller.service.EmployeeService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/employees")
//public class EmployeeController {
//
//
//    private final EmployeeService service;
//
//    public EmployeeController(EmployeeService service) {
//        this.service = service;
//    }
//
//
//    @GetMapping
//    public List<Employee> getEmployees() {
//        return service.getAllEmployees();
//    }
//
//    @PostMapping
//    public Employee createEmployee(@RequestBody Employee employee) {
//        return service.saveEmployee(employee);
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteEmployee(@PathVariable Long id) {
//        service.deleteEmployee(id);
//    }
//}