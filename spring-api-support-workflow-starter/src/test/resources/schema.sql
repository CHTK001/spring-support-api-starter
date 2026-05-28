CREATE TABLE IF NOT EXISTS wf_def (
    wf_def_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_def_name VARCHAR(200) NOT NULL,
    wf_def_code VARCHAR(100) NOT NULL UNIQUE,
    wf_def_category VARCHAR(100),
    wf_def_version INT DEFAULT 1,
    wf_def_status INT DEFAULT 0 COMMENT '0=draft 1=published 2=disabled',
    wf_def_form_config TEXT,
    wf_def_remark VARCHAR(500),
    create_name VARCHAR(100),
    create_by INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NULL,
    update_name VARCHAR(100),
    update_by INT
);

CREATE TABLE IF NOT EXISTS wf_def_node (
    wf_def_node_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_def_id INT NOT NULL,
    node_name VARCHAR(200) NOT NULL,
    node_type VARCHAR(50) NOT NULL,
    node_config TEXT,
    assignee_type VARCHAR(50),
    assignee_value VARCHAR(500),
    node_sort INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS wf_def_edge (
    wf_def_edge_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_def_id INT NOT NULL,
    edge_name VARCHAR(200),
    source_node_id INT NOT NULL,
    target_node_id INT NOT NULL,
    edge_condition TEXT
);

CREATE TABLE IF NOT EXISTS wf_inst (
    wf_inst_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_def_id INT NOT NULL,
    business_type VARCHAR(100),
    business_id VARCHAR(100),
    inst_title VARCHAR(500) NOT NULL,
    form_data TEXT,
    starter_id INT NOT NULL,
    dept_id INT,
    inst_status VARCHAR(50) DEFAULT 'DRAFT',
    idempotent_key VARCHAR(200) UNIQUE,
    current_node_id INT,
    expire_time TIMESTAMP NULL,
    finish_time TIMESTAMP NULL,
    create_name VARCHAR(100),
    create_by INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NULL,
    update_name VARCHAR(100),
    update_by INT
);

CREATE TABLE IF NOT EXISTS wf_task (
    wf_task_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_inst_id INT NOT NULL,
    wf_def_node_id INT,
    node_name VARCHAR(200),
    assignee_id INT NOT NULL,
    assignee_name VARCHAR(100),
    task_status VARCHAR(50) DEFAULT 'PENDING',
    comment VARCHAR(2000),
    attachment TEXT,
    deadline TIMESTAMP NULL,
    form_data TEXT,
    sort INT DEFAULT 0,
    create_name VARCHAR(100),
    create_by INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NULL,
    update_name VARCHAR(100),
    update_by INT
);

CREATE TABLE IF NOT EXISTS wf_task_history (
    wf_task_history_id INT AUTO_INCREMENT PRIMARY KEY,
    wf_inst_id INT NOT NULL,
    wf_task_id INT,
    operator_id INT NOT NULL,
    operator_name VARCHAR(100),
    operation VARCHAR(50) NOT NULL,
    comment VARCHAR(2000),
    duration_seconds BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
