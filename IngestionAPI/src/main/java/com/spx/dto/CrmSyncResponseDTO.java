package com.spx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class CrmSyncResponseDTO {

    private String status;
    private int campaignsReceived;
    private String message;
    private Instant timestamp;

}