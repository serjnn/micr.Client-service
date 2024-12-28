package com.serjnn.ClientService.dtos;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderDTO {
    private UUID orderId;
    private Long clientId;
    private List<BucketItemDTO> items;
    private BigDecimal totalSum;

}