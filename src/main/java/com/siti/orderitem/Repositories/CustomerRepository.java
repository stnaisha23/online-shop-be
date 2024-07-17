package com.siti.orderitem.Repositories;

import com.siti.orderitem.Models.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByIsActive(boolean isActive, Pageable pageable);
    Page<Customer> findByCustomerNameContainingIgnoreCaseAndIsActive(String customerName, boolean isActive, Pageable pageable);
    Optional<Customer> findByCustomerId(Long customerId);
    boolean existsByCustomerPhone(String customerPhone);

    Page<Customer> findAllBy(Pageable pageable);
    Page<Customer> findAllByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
}
