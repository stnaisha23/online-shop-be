package com.siti.orderitem.Controllers;

import com.siti.orderitem.DTO.OrderDTO;
import com.siti.orderitem.DTO.Requests.OrderRequest;
import com.siti.orderitem.DTO.Responses.ApiResponse;
import com.siti.orderitem.Models.Orders;
import com.siti.orderitem.Services.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/order-items/orders")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Page<OrderDTO> ordersPage = ordersService.getAllOrders(page, size)
                .map(order -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setOrderId(order.getOrderId());
                    dto.setOrderCode(order.getOrderCode());
                    dto.setOrderDate(order.getOrderDate());
                    dto.setTotalPrice(order.getTotalPrice());
                    dto.setCustomerId(order.getCustomer().getCustomerId());
                    dto.setItemsId(order.getItems().getItemsId());
                    dto.setQuantity(order.getQuantity());
                    return dto;
                });

        if (ordersPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(0, 0, null, HttpStatus.NOT_FOUND, "Data Orders Tidak Ada!"));
        }

        ApiResponse response = new ApiResponse(
                ordersPage.getTotalPages(),
                ordersPage.getContent().size(),
                ordersPage.getContent(),
                HttpStatus.OK,
                "Orders Retrieved Successfully!"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orders> getOrderById(@PathVariable Long id) {
        Optional<Orders> order = ordersService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
public ResponseEntity<ApiResponse> addOrder(@RequestBody OrderRequest orderRequest) {
    try {
        if (orderRequest.getCustomerId() == null || orderRequest.getItemsId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, "CustomerId and ItemId must not be null"));
        }

        Orders newOrder = new Orders();
        newOrder.setQuantity(orderRequest.getQuantity());
        Orders createdOrder = ordersService.addOrder(newOrder, orderRequest.getCustomerId(), orderRequest.getItemsId());

        ApiResponse response = new ApiResponse(
                1,
                1,
                createdOrder,
                HttpStatus.OK,
                "Order Added Successfully!"
        );

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, e.getMessage()));
    }
}


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequest orderRequest) {

        try {
            Orders updatedOrder = new Orders();
            updatedOrder.setQuantity(orderRequest.getQuantity());
            Orders order = ordersService.updateOrder(id, updatedOrder);

            if (order != null) {
                ApiResponse response = new ApiResponse(
                        1,
                        1,
                        order,
                        HttpStatus.OK,
                        "Order Updated Successfully!"
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
    public ResponseEntity<ApiResponse> deleteOrder(@PathVariable Long id) {
        try {
            ordersService.deleteOrder(id);

            ApiResponse response = new ApiResponse(
                    1,
                    1,
                    null,
                    HttpStatus.OK,
                    "Order Deleted Successfully!"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(0, 0, null, HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
