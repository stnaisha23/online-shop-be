package com.siti.orderitem.DTO;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long orderId;
    private String orderCode;
    private Date orderDate;
    private Double totalPrice;
    private Long customerId;
    private Long itemsId;
    private Double quantity;

    // Constructor, getters, and setters
}
