package com.ibpms.poc.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class KanbanBoardDto {
    private List<KanbanColumnDto> columns;
}
