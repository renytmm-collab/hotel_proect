-- Run using the owning Oracle schema, READ ONLY. Do not include customer rows.
SELECT table_name FROM user_tables ORDER BY table_name;
SELECT table_name, column_id, column_name, data_type, data_length, char_length,
       data_precision, data_scale, nullable, data_default
FROM user_tab_columns ORDER BY table_name, column_id;
SELECT c.table_name, c.constraint_name, c.constraint_type, c.status,
       c.r_constraint_name, c.delete_rule, cc.column_name, cc.position
FROM user_constraints c LEFT JOIN user_cons_columns cc
ON cc.constraint_name = c.constraint_name
ORDER BY c.table_name, c.constraint_name, cc.position;
SELECT table_name, index_name, uniqueness FROM user_indexes ORDER BY table_name, index_name;
SELECT table_name, index_name, column_name, column_position
FROM user_ind_columns ORDER BY table_name, index_name, column_position;
SELECT trigger_name, table_name, status, triggering_event, trigger_body FROM user_triggers;
SELECT sequence_name, min_value, max_value, increment_by, last_number FROM user_sequences;
SELECT view_name FROM user_views;
