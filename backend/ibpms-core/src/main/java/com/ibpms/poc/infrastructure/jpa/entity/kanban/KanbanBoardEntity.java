package com.ibpms.poc.infrastructure.jpa.entity.kanban;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ibpms_kanban_board")
public class KanbanBoardEntity {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "wip_limit")
    private Integer wipLimit;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getWipLimit() { return wipLimit; }
    public void setWipLimit(Integer wipLimit) { this.wipLimit = wipLimit; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
