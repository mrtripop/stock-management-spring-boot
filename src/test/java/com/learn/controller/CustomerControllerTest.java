package com.learn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCustomer() {
    }

    @Test
    void insertNewCustomer() {
    }

    @Test
    void deleteCustomer() {
    }

    @Test
    void updateCustomer() {
    }
}