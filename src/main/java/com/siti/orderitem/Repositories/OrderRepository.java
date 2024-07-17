package com.siti.orderitem.Repositories;

import com.siti.orderitem.Models.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findAll(Pageable pageable);

    @Query("SELECT o FROM Orders o JOIN FETCH o.customer JOIN FETCH o.items")
    Page<Orders> findAllWithCustomerAndItems(Pageable pageable);
}
