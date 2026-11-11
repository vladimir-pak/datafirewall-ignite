package com.gpb.datafirewall.ignite.dto;

import lombok.Data;

@Data
public class RequestJwtDto {
    private String secret;
    private String service;
}
