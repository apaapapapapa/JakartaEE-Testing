-- 親タスク（トップレベル）
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (1, 'Project Alpha', '2025-08-01', false, NULL);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (2, 'Project Beta', '2025-08-05', true, NULL);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (3, 'Project Gamma', '2025-08-10', false, NULL);

-- サブタスク（Project Alpha の子）
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (4, 'Alpha - Design Phase', '2025-08-03', false, 1);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (5, 'Alpha - Development Phase', '2025-08-10', false, 1);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (6, 'Alpha - Testing Phase', '2025-08-15', false, 1);

-- サブタスク（Project Beta の子）
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (7, 'Beta - Requirements', '2025-08-06', true, 2);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (8, 'Beta - Review', '2025-08-07', true, 2);

-- サブタスク（Project Gamma の子）
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (9, 'Gamma - Planning', '2025-08-11', false, 3);
INSERT INTO Task (id, title, dueDate, completed, parent_id) VALUES (10, 'Gamma - Kickoff', '2025-08-12', false, 3);

ALTER TABLE Task ALTER COLUMN id RESTART WITH 11;
