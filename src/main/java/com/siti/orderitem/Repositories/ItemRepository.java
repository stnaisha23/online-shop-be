package com.siti.orderitem.Repositories;

import com.siti.orderitem.Models.Items;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Items, Long> {
    Page<Items> findByIsAvailable(boolean isAvailable, Pageable pageable);

    Page<Items> findByItemsNameContainingIgnoreCaseAndIsAvailable(String itemsName, boolean isAvailable, Pageable pageable);
    
    Optional<Items> findByItemsId(Long itemsId);

    Page<Items> findAll(Pageable pageable);

    Page<Items> findByItemsNameContainingIgnoreCase(String itemsName, Pageable pageable);
}
