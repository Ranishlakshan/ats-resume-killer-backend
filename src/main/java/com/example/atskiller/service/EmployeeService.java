//package com.example.atskiller.service;
//
//import com.example.atskiller.model.Employee;
//import com.example.atskiller.repository.EmployeeRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class EmployeeService {
//
//    private final EmployeeRepository repository;
//
//    public EmployeeService(EmployeeRepository repository) {
//        this.repository = repository;
//    }
//
//    public List<Employee> getAllEmployees() {
//        return repository.findAll();
//    }
//
//    public Employee saveEmployee(Employee employee) {
//        return repository.save(employee);
//    }
//
//    public void deleteEmployee(Long id) {
//        repository.deleteById(id);
//    }
//}