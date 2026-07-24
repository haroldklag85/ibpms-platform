package com.ibpms.poc.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class KanbanColumnDto {
    private String name;
    private List<KanbanTaskDto> tasks;
}
