package com.siti.orderitem.DTO.Requests;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRequest {
    private String customer_name;
    private String customer_address;
    private String customer_code;
    private String customer_phone;
    private Boolean is_active;
    private Date last_order_date;
    private String pic;
}

