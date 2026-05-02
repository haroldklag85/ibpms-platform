package com.ibpms.poc.domain.model.kanban;

import java.util.UUID;

public class KanbanColumn {
    private UUID id;
    private UUID boardId;
    private String name;
    private int position;

    public KanbanColumn(UUID id, UUID boardId, String name, int position) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
    }

    public KanbanColumn() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBoardId() { return boardId; }
    public void setBoardId(UUID boardId) { this.boardId = boardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
