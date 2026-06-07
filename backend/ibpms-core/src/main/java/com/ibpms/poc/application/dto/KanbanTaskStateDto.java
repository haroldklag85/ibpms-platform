package com.ibpms.poc.application.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KanbanTaskStateDto {
    private String id;
    private String status;
    private Long version;
}
