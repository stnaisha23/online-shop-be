package com.siti.orderitem.DTO.Responses;

import lombok.Data;

import java.util.List;

import com.siti.orderitem.Models.Customer;

@Data
public class CustomerResponse {
    private List<Customer> data;
    private String status;
    private String message;
}


