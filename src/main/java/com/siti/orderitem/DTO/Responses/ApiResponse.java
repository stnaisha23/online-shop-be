package com.siti.orderitem.DTO.Responses;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private int pages;
    private long totalData;
    private Object Data;
    private HttpStatus status;
    private String message;

    // constructors, getters, and setters
}

