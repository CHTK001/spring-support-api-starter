-- 学校信息表
CREATE TABLE `school_info`
(
    `school_id`               bigint(20) NOT NULL AUTO_INCREMENT COMMENT '学校ID',
    `school_code`             varchar(50)  DEFAULT NULL COMMENT '学校代码',
    `school_name`             varchar(100) DEFAULT NULL COMMENT '学校名称',
    `school_english_name`     varchar(200) DEFAULT NULL COMMENT '学校英文名',
    `school_type`             varchar(50)  DEFAULT NULL COMMENT '学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）',
    `school_province_id`      varchar(50)  DEFAULT NULL COMMENT '学校所在省份ID',
    `school_city_id`          varchar(50)  DEFAULT NULL COMMENT '学校所在城市ID',
    `school_district_id`      varchar(50)  DEFAULT NULL COMMENT '学校所在区县ID',
    `school_address`          varchar(255) DEFAULT NULL COMMENT '学校地址',
    `school_create_time`      varchar(50)  DEFAULT NULL COMMENT '学校创建时间',
    `school_education_level`  varchar(50)  DEFAULT NULL COMMENT '学校办学层次',
    `school_education_native` varchar(50)  DEFAULT NULL COMMENT '学校办学性质',
    `school_subordinate_to`   varchar(100) DEFAULT NULL COMMENT '学校主管部门',
    `school_is_985`           int(1)       DEFAULT '0' COMMENT '是否985院校(0:否,1:是)',
    `school_dual`             varchar(50)  DEFAULT NULL COMMENT '学校双一流等级',
    `school_is_211`           int(1)       DEFAULT '0' COMMENT '是否211院校(0:否,1:是)',
    `school_is_double_first`  int(1)       DEFAULT '0' COMMENT '是否是双一流（0-否，1-是）',
    `school_website_url`      varchar(255) DEFAULT NULL COMMENT '学校网站URL',
    `school_phone`            varchar(50)  DEFAULT NULL COMMENT '学校电话',
    `school_post_code`        varchar(20)  DEFAULT NULL COMMENT '学校邮编',
    `school_remark`           varchar(500) DEFAULT NULL COMMENT '备注',
    `school_founding_year`    varchar(20)  DEFAULT NULL COMMENT '建校时间',
    `school_introduction`     text         DEFAULT NULL COMMENT '学校简介',
    `school_features`         text         DEFAULT NULL COMMENT '学校特色',
    `school_logo_url`         varchar(255) DEFAULT NULL COMMENT '学校logo URL',
    `school_badge_url`        varchar(255) DEFAULT NULL COMMENT '学校校徽 URL',
    `school_admission_url`    varchar(255) DEFAULT NULL COMMENT '招生网址',
    `school_admission`        int(11)      DEFAULT NULL COMMENT '学校录取批次',
    `school_double_high`      int(1)       DEFAULT '0' COMMENT '双高计划: 0（非双高院校）',
    `school_employment_rate`  varchar(20)  DEFAULT NULL COMMENT '就业率',
    `school_tag`              varchar(255) DEFAULT NULL COMMENT '学校标签',
    `school_view_month`       int(11)      DEFAULT '0' COMMENT '学校月访问量',
    `school_view_total`       int(11)      DEFAULT '0' COMMENT '学校总访问量',
    `school_rank`             int(11)      DEFAULT NULL COMMENT '学校排名',
    `school_num_doctor`       int(11)      DEFAULT '0' COMMENT '博士点数量',
    `school_num_master`       int(11)      DEFAULT '0' COMMENT '硕士点数量',
    `school_num_subject`      int(11)      DEFAULT '0' COMMENT '学科数量',
    `school_gaokao_url`       varchar(255) DEFAULT NULL COMMENT '高考网链接',
    `school_content`          text         DEFAULT NULL COMMENT '学校简介',
    `school_email`            varchar(100) DEFAULT NULL COMMENT '学校邮箱',
    `school_deleted`          int(1)       DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`             datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`             varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`               varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`               varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`             datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_id`),
    UNIQUE KEY `idx_school_code` (`school_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校信息表';

-- 学校招生计划表
CREATE TABLE `school_admission_plan`
(
    `school_admission_plan_id`                   bigint(20) NOT NULL AUTO_INCREMENT COMMENT '招生计划ID',
    `school_admission_plan_school_id`            bigint(20)   DEFAULT NULL COMMENT '学校ID',
    `school_admission_plan_school_code`          varchar(50)  DEFAULT NULL COMMENT '学校代码',
    `school_admission_plan_name`                 varchar(100) DEFAULT NULL COMMENT '招生计划名称',
    `school_admission_plan_remark`               varchar(500) DEFAULT NULL COMMENT '招生计划备注',
    `school_admission_plan_year`                 int(11)      DEFAULT NULL COMMENT '招生计划年份',
    `school_admission_plan_province`             varchar(50)  DEFAULT NULL COMMENT '招生计划省份',
    `school_admission_plan_batch`                varchar(50)  DEFAULT NULL COMMENT '招生计划批次',
    `school_admission_plan_major_name`           varchar(100) DEFAULT NULL COMMENT '招生计划专业名称',
    `school_admission_plan_number`               int(11)      DEFAULT NULL COMMENT '招生计划人数',
    `school_admission_plan_tuition`              double       DEFAULT NULL COMMENT '招生计划学费',
    `school_admission_plan_length`               int(11)      DEFAULT NULL COMMENT '招生计划学制',
    `school_admission_plan_subject_requirements` varchar(255) DEFAULT NULL COMMENT '招生计划科目要求',
    `school_admission_plan_major_id`             varchar(50)  DEFAULT NULL COMMENT '专业ID',
    `school_admission_plan_major_code`           varchar(50)  DEFAULT NULL COMMENT '专业代码',
    `school_admission_plan_actual_count`         int(11)      DEFAULT NULL COMMENT '实际招生人数',
    `school_admission_plan_subject_type`         varchar(50)  DEFAULT NULL COMMENT '科目类型（理科、文科、综合）',
    `school_admission_plan_plan_count`           int(11)      DEFAULT NULL COMMENT '计划招生人数',
    `school_admission_plan_admission_type`       varchar(50)  DEFAULT NULL COMMENT '招生类型（普通类、国家专项等）',
    `school_admission_plan_deleted`              int(1)       DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                                datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`                                varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`                                  varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`                                  varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`                                datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`                                varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_admission_plan_id`),
    KEY `idx_school_code` (`school_admission_plan_school_code`),
    KEY `idx_major_code` (`school_admission_plan_major_code`),
    KEY `idx_year_province` (`school_admission_plan_year`, `school_admission_plan_province`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校招生计划表';

-- 学校历史分数线表
CREATE TABLE `school_historical_score`
(
    `school_historical_score_id`              bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史分数线ID',
    `school_historical_score_school_id`       bigint(20)   DEFAULT NULL COMMENT '学校ID',
    `school_historical_score_school_code`     varchar(50)  DEFAULT NULL COMMENT '学校代码',
    `school_historical_score_major_id`        bigint(20)   DEFAULT NULL COMMENT '专业ID',
    `school_historical_score_major_code`      varchar(50)  DEFAULT NULL COMMENT '专业代码',
    `school_historical_score_year`            int(11)      DEFAULT NULL COMMENT '年份',
    `school_historical_score_province`        varchar(50)  DEFAULT NULL COMMENT '省份',
    `school_historical_score_subject_type`    varchar(50)  DEFAULT NULL COMMENT '科目类型',
    `school_historical_score_batch`           varchar(50)  DEFAULT NULL COMMENT '批次',
    `school_historical_score_admission_type`  varchar(50)  DEFAULT NULL COMMENT '录取类型',
    `school_historical_score_max_score`       int(11)      DEFAULT NULL COMMENT '最高分',
    `school_historical_score_min_score`       int(11)      DEFAULT NULL COMMENT '最低分',
    `school_historical_score_avg_score`       int(11)      DEFAULT NULL COMMENT '平均分',
    `school_historical_score_admission_count` int(11)      DEFAULT NULL COMMENT '录取人数',
    `school_historical_score_provincial_line` int(11)      DEFAULT NULL COMMENT '省控线',
    `school_historical_score_lowest_rank`     int(11)      DEFAULT NULL COMMENT '最低位次',
    `school_historical_score_applied_count`   int(11)      DEFAULT NULL COMMENT '报名人数',
    `school_historical_score_admission_rate`  double       DEFAULT NULL COMMENT '录取率',
    `school_historical_score_remark`          varchar(500) DEFAULT NULL COMMENT '备注',
    `school_historical_score_deleted`         int(1)       DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                             datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`                             varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`                               varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`                               varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`                             datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`                             varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_historical_score_id`),
    KEY `idx_school_code` (`school_historical_score_school_code`),
    KEY `idx_major_code` (`school_historical_score_major_code`),
    KEY `idx_year_province` (`school_historical_score_year`, `school_historical_score_province`),
    KEY `idx_subject_batch` (`school_historical_score_subject_type`, `school_historical_score_batch`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校历史分数线表';

-- 学校专业表
CREATE TABLE `school_major`
(
    `school_major_id`                bigint(20) NOT NULL AUTO_INCREMENT COMMENT '专业ID',
    `school_code`                    varchar(50)  DEFAULT NULL COMMENT '学校代码',
    `school_major_code`              varchar(50)  DEFAULT NULL COMMENT '专业代码',
    `school_major_name`              varchar(100) DEFAULT NULL COMMENT '专业名称',
    `school_major_type`              varchar(50)  DEFAULT NULL COMMENT '专业类型',
    `school_major_type_name`         varchar(100) DEFAULT NULL COMMENT '专业类型名称',
    `school_major_level`             varchar(50)  DEFAULT NULL COMMENT '专业层次',
    `school_major_category_code`     varchar(50)  DEFAULT NULL COMMENT '专业类别代码',
    `school_major_category_name`     varchar(100) DEFAULT NULL COMMENT '专业类别名称',
    `school_major_parent_code`       varchar(50)  DEFAULT NULL COMMENT '父级专业代码',
    `school_major_category_label`    varchar(100) DEFAULT NULL COMMENT '专业类别标签',
    `school_major_year`              varchar(20)  DEFAULT NULL COMMENT '专业年份',
    `school_major_content`           text         DEFAULT NULL COMMENT '专业内容',
    `school_major_course`            text         DEFAULT NULL COMMENT '专业课程',
    `school_major_employment`        text         DEFAULT NULL COMMENT '就业方向',
    `school_major_ruan_ke_level`     varchar(50)  DEFAULT NULL COMMENT '软科等级',
    `school_major_ruan_ke_rank`      int(11)      DEFAULT NULL COMMENT '软科排名',
    `school_major_xue_ke_rank`       int(11)      DEFAULT NULL COMMENT '学科排名',
    `school_major_xue_ke_rank_score` int(11)      DEFAULT NULL COMMENT '学科排名分数',
    `create_time`                    datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`                    varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`                      varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`                      varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`                    datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`                    varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_major_id`),
    UNIQUE KEY `idx_school_major_code` (`school_code`, `school_major_code`),
    KEY `idx_major_name` (`school_major_name`),
    KEY `idx_major_type` (`school_major_type`),
    KEY `idx_major_level` (`school_major_level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校专业表';

-- 学校排名表
CREATE TABLE `school_rank`
(
    `school_rank_id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '排名ID',
    `school_code`             varchar(50)  DEFAULT NULL COMMENT '学校代码',
    `school_rank_value`       int(11)      DEFAULT NULL COMMENT '排名值',
    `school_rank_type`        varchar(50)  DEFAULT NULL COMMENT '排名类型',
    `school_rank_name`        varchar(100) DEFAULT NULL COMMENT '排名名称',
    `school_rank_year`        int(11)      DEFAULT NULL COMMENT '排名年份',
    `school_rank_institution` varchar(100) DEFAULT NULL COMMENT '排名机构',
    `school_rank_score`       double       DEFAULT NULL COMMENT '排名得分',
    `school_rank_change`      int(11)      DEFAULT NULL COMMENT '排名变化',
    `school_rank_remark`      varchar(500) DEFAULT NULL COMMENT '排名备注',
    `school_rank_deleted`     int(1)       DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`             datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`             varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`               varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`               varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`             datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`             varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_rank_id`),
    KEY `idx_school_code` (`school_code`),
    KEY `idx_rank_type_year` (`school_rank_type`, `school_rank_year`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校排名表';

-- 学校同步配置表
CREATE TABLE `school_sync_config`
(
    `school_sync_config_id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_sync_config_name`        varchar(100) DEFAULT NULL COMMENT '配置名称',
    `school_sync_config_type`        varchar(50)  DEFAULT NULL COMMENT '配置类型',
    `school_sync_config_sync_type`   varchar(50)  DEFAULT NULL COMMENT '同步类型',
    `school_sync_config_cookie`      text COMMENT 'cookie',
    `school_sync_config_sync_status` int(1)       DEFAULT '0' COMMENT '同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）',
    `school_sync_config_enabled`     tinyint(1)   DEFAULT '0' COMMENT '是否启用',
    `school_sync_config_sync_time`   datetime     DEFAULT NULL COMMENT '同步时间',
    `school_sync_config_deleted`     int(1)       DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                    datetime     DEFAULT NULL COMMENT '创建时间',
    `create_name`                    varchar(64)  DEFAULT NULL COMMENT '创建者',
    `create_by`                      varchar(64)  DEFAULT NULL COMMENT '创建者',
    `update_by`                      varchar(64)  DEFAULT NULL COMMENT '更新者',
    `update_time`                    datetime     DEFAULT NULL COMMENT '更新时间',
    `update_name`                    varchar(64)  DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_sync_config_id`),
    KEY `idx_config_type` (`school_sync_config_type`),
    KEY `idx_sync_status` (`school_sync_config_sync_status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='学校同步配置表';
CREATE TABLE `school_dict`
(
    `school_dict_id`   int(11) NOT NULL AUTO_INCREMENT,
    `school_dict_code` varchar(255)                                                 DEFAULT NULL COMMENT '字典代码',
    `school_dict_name` varchar(255)                                                 DEFAULT NULL COMMENT '字典名称',
    `school_dict_type` varchar(255)                                                 DEFAULT NULL COMMENT '字典类型',
    `create_time`      datetime                                                     DEFAULT NULL COMMENT '创建时间',
    `create_name`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
    `create_by`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
    `update_by`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
    `update_time`      datetime                                                     DEFAULT NULL COMMENT '更新时间',
    `update_name`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
    PRIMARY KEY (`school_dict_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT =' 学校字典实体类 用于存储学校相关的字典数据，如学校类型、办学层次、主管部门等';