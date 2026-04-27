ALTER TABLE spider_definition ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_definition ADD COLUMN create_by INT NULL;
ALTER TABLE spider_definition ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_definition ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_definition ADD COLUMN update_by INT NULL;
ALTER TABLE spider_definition ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_flow ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_flow ADD COLUMN create_by INT NULL;
ALTER TABLE spider_flow ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_flow ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_flow ADD COLUMN update_by INT NULL;
ALTER TABLE spider_flow ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_flow_node ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_flow_node ADD COLUMN create_by INT NULL;
ALTER TABLE spider_flow_node ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_flow_node ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_flow_node ADD COLUMN update_by INT NULL;
ALTER TABLE spider_flow_node ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_url_queue ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_url_queue ADD COLUMN create_by INT NULL;
ALTER TABLE spider_url_queue ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_url_queue ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_url_queue ADD COLUMN update_by INT NULL;
ALTER TABLE spider_url_queue ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_parse_result ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_parse_result ADD COLUMN create_by INT NULL;
ALTER TABLE spider_parse_result ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_parse_result ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_parse_result ADD COLUMN update_by INT NULL;
ALTER TABLE spider_parse_result ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_execution ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_execution ADD COLUMN create_by INT NULL;
ALTER TABLE spider_execution ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_execution ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_execution ADD COLUMN update_by INT NULL;
ALTER TABLE spider_execution ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_execution_step ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_execution_step ADD COLUMN create_by INT NULL;
ALTER TABLE spider_execution_step ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_execution_step ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_execution_step ADD COLUMN update_by INT NULL;
ALTER TABLE spider_execution_step ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_manual_task ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_manual_task ADD COLUMN create_by INT NULL;
ALTER TABLE spider_manual_task ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_manual_task ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_manual_task ADD COLUMN update_by INT NULL;
ALTER TABLE spider_manual_task ADD COLUMN update_time DATETIME NULL;

ALTER TABLE spider_ai_template ADD COLUMN create_name VARCHAR(255) NULL;
ALTER TABLE spider_ai_template ADD COLUMN create_by INT NULL;
ALTER TABLE spider_ai_template ADD COLUMN create_time DATETIME NULL;
ALTER TABLE spider_ai_template ADD COLUMN update_name VARCHAR(255) NULL;
ALTER TABLE spider_ai_template ADD COLUMN update_by INT NULL;
ALTER TABLE spider_ai_template ADD COLUMN update_time DATETIME NULL;
