-- liquibase formatted sql

-- changeset antigravity:064-fix-kanban-board-v2-seed context:dev,test
-- comment: Fix kanban orphan rows by migrating seed data to v2 board table before Hibernate adds FK constraint

INSERT INTO ibpms_kanban_board_v2 (id, title, wip_limit, order_index)
SELECT id, title, wip_limit, order_index FROM ibpms_kanban_board
ON CONFLICT (id) DO NOTHING;
