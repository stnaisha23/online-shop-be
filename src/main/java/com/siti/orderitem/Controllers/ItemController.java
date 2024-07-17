package com.siti.orderitem.Controllers;

import com.siti.orderitem.DTO.Responses.ApiResponse;
import com.siti.orderitem.Models.Items;
import com.siti.orderitem.Services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/order-items/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "itemsName") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String itemsName) {

        Page<Items> itemsPage = itemService.getAllItems(page, size, sortBy, direction, itemsName);

        if (itemsPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(0, 0, null, HttpStatus.NOT_FOUND, "Tidak Ada Items!"));
        }

        int totalData = itemsPage.getContent().size();

        ApiResponse response = new ApiResponse(
            itemsPage.getTotalPages(),
            totalData,
            itemsPage.getContent(),
            HttpStatus.OK,
            "Data Berhasil Ditampilkan!"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAllAvailableItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "itemsName") String sortBy,
        @RequestParam(defaultValue = "asc") String direction,
        @RequestParam(required = false) String itemsName) {

        Page<Items> availableItems = itemService.getAllAvailableItems(page, size, sortBy, direction, itemsName);

        if (availableItems.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(0, 0, null, HttpStatus.NOT_FOUND, "Tidak Ada Items Tersedia!"));
        }

        int totalData = availableItems.getContent().size();

        ApiResponse response = new ApiResponse(
            availableItems.getTotalPages(),
            totalData,
            availableItems.getContent(),
            HttpStatus.OK,
            "Data Berhasil Ditampilkan!"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Items> getItemById(@PathVariable Long id) {
        Optional<Items> item = itemService.getItemById(id);
        return item.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addItem(
            @RequestParam("itemsName") String itemsName,
            @RequestParam("stock") Double stock,
            @RequestParam("price") Double price,
            @RequestParam(value = "isAvailable", defaultValue = "true") Boolean isAvailable,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date lastReStock) {
        try {
            Items newItem = new Items();
            newItem.setItemsName(itemsName);
            newItem.setStock(stock);
            newItem.setPrice(price);
            newItem.setIsAvailable(isAvailable);
            newItem.setLastReStock(lastReStock);

            Items createdItem = itemService.addItem(newItem);

            ApiResponse response = new ApiResponse(
                1,
                1,
                createdItem,
                HttpStatus.OK,
                "Data Berhasil Ditambahkan!"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateItemById(
            @PathVariable Long id,
            @RequestParam(required = false) String itemsName,
            @RequestParam(required = false) Double stock,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date lastReStock) {

        Items updatedItem = new Items();
        updatedItem.setItemsName(itemsName);
        updatedItem.setStock(stock);
        updatedItem.setPrice(price);
        updatedItem.setIsAvailable(isAvailable);
        updatedItem.setLastReStock(lastReStock);

        try {
            Items item = itemService.updateItemById(id, updatedItem);

            if (item != null) {
                ApiResponse response = new ApiResponse(
                    1,
                    1,
                    item,
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
    public ResponseEntity<ApiResponse> deleteItemById(@PathVariable Long id) {
        try {
            itemService.deleteItemById(id);

            ApiResponse response = new ApiResponse(
                1,
                1,
                null,
                HttpStatus.OK,
                "Data Berhasil Dihapus!"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse(
                0,
                0,
                null,
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(
                0,
                0,
                null,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi kesalahan saat menghapus data"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
