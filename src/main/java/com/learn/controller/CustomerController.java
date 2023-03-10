package com.learn.controller;

import com.learn.model.postgres.Customer;
import com.learn.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomer() {
        return customerService.getAllCustomer();
    }

    @PostMapping
    public Customer insertNewCustomer(@RequestBody Customer customer) {
        return customerService.insertCustomer(customer);
    }

    @DeleteMapping("{customerId}")
    public boolean deleteCustomer(@PathVariable("customerId") Integer customerId) {
        return customerService.deleteCustomer(customerId);
    }

    @PutMapping("{customerId}")
    public Customer updateCustomer(@PathVariable("customerId") Integer customerId, @RequestBody Customer customer) {
        return customerService.updateCustomerName(customerId, customer);
    }
}
