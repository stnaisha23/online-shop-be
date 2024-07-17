package com.siti.orderitem.Controllers;

import com.siti.orderitem.DTO.Responses.ApiResponse;
import com.siti.orderitem.Models.Customer;
import com.siti.orderitem.Services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/order-items/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getAllActiveCustomers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "customerName") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String customerName) {

        Page<Customer> activeCustomers = customerService.getAllActiveCustomers(page, size, sortBy, direction, customerName);

        if (activeCustomers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(0, 0, null, HttpStatus.NOT_FOUND, "Tidak Ada Customer Aktif!"));
        }

        // int pages = activeCustomers.getTotalPages();
        int totalData = activeCustomers.getContent().size();

        ApiResponse response = new ApiResponse(
            activeCustomers.getTotalPages(),
            totalData, // Pass totalData here
            activeCustomers.getContent(),
            HttpStatus.OK,
            "Data Berhasil Ditampilkan!"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
public ResponseEntity<ApiResponse> getAllCustomers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "5") int size,
    @RequestParam(defaultValue = "customerName") String sortBy,
    @RequestParam(defaultValue = "asc") String direction,
    @RequestParam(required = false) String customerName) {

    Page<Customer> allCustomers = customerService.getAllCustomers(page, size, sortBy, direction, customerName);

    if (allCustomers.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(0, 0, null, HttpStatus.NOT_FOUND, "Tidak Ada Customer!"));
    }

    int pages = allCustomers.getTotalPages(); // Total pages
    long totalElements = allCustomers.getTotalElements(); // Total data count

    ApiResponse response = new ApiResponse(
        pages,
        totalElements,
        allCustomers.getContent(),
        HttpStatus.OK,
        "Data Berhasil Ditampilkan!"
    );

    return ResponseEntity.ok(response);
}



    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addCustomer(
            @RequestParam("customerName") String customerName,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam("customerAddress") String customerAddress,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastOrderDate,
            @RequestParam(required = false) MultipartFile pic) {
        try {
            Customer newCustomer = new Customer();
            newCustomer.setCustomerName(customerName);
            newCustomer.setCustomerPhone(customerPhone);
            newCustomer.setCustomerAddress(customerAddress);
            newCustomer.setIsActive(isActive);
            newCustomer.setLastOrderDate(lastOrderDate);

            Customer createdCustomer = customerService.addCustomer(newCustomer, pic);

            ApiResponse response = new ApiResponse(
                1,
                1,
                createdCustomer,
                HttpStatus.OK,
                "Data Berhasil Ditambahkan!"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCustomerById(
            @PathVariable Long id,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerAddress,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date lastOrderDate,
            @RequestParam(required = false) MultipartFile pic) {

        Customer updatedCustomer = new Customer();
        updatedCustomer.setCustomerName(customerName);
        updatedCustomer.setCustomerAddress(customerAddress);
        updatedCustomer.setCustomerCode(customerCode);
        updatedCustomer.setCustomerPhone(customerPhone);
        updatedCustomer.setIsActive(isActive);
        updatedCustomer.setLastOrderDate(lastOrderDate);

        try {
            Customer customer = customerService.updateCustomerById(id, updatedCustomer, pic);

            if (customer != null) {
                ApiResponse response = new ApiResponse(
                    1,
                    1,
                    customer,
                    HttpStatus.OK,
                    "Data Berhasil Diupdate!"
                );

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCustomerById(@PathVariable Long id) {
        try {
            customerService.deleteCustomerId(id);

            ApiResponse response = new ApiResponse(
                1,
                1,
                null,
                HttpStatus.OK,
                "Data Berhasil Dihapus!"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
