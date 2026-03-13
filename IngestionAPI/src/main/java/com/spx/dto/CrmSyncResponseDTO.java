package com.spx.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class CrmSyncResponseDTO {

    private String status;
    private String batchId;
    private int campaignsReceived;
    private String message;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "dd-MM-yyyy HH:mm:ss"
    )

    private LocalDateTime timestamp;

}