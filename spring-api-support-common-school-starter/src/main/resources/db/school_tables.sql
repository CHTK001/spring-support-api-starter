-- 学校信息表
CREATE TABLE `school_info`
(
    `school_id`              bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_code`            varchar(50)  NOT NULL COMMENT '学校代码',
    `school_name`            varchar(100) NOT NULL COMMENT '学校名称',
    `school_english_name`    varchar(100)   DEFAULT NULL COMMENT '学校英文名',
    `school_type`            varchar(20)    DEFAULT NULL COMMENT '学校类型（综合、理工、农林、医药、师范、语言、财经、政法、体育、艺术等）',
    `school_province`        varchar(50)    DEFAULT NULL COMMENT '所在省份',
    `school_city`            varchar(50)    DEFAULT NULL COMMENT '所在城市',
    `school_district`        varchar(50)    DEFAULT NULL COMMENT '所在区县',
    `school_address`         varchar(255)   DEFAULT NULL COMMENT '详细地址',
    `school_education_level` varchar(50)    DEFAULT NULL COMMENT '办学层次（本科、专科）',
    `school_subordinate_to`  varchar(50)    DEFAULT NULL COMMENT '隶属于（教育部、地方等）',
    `school_phone`           varchar(20)    DEFAULT NULL COMMENT '联系电话',
    `school_email`           varchar(100)   DEFAULT NULL COMMENT '电子邮箱',
    `school_website`         varchar(255)   DEFAULT NULL COMMENT '学校官网',
    `school_founded_year`    int(4)         DEFAULT NULL COMMENT '建校年份',
    `school_area`            decimal(10, 2) DEFAULT NULL COMMENT '占地面积（亩）',
    `school_student_count`   int(11)        DEFAULT NULL COMMENT '在校生人数',
    `school_teacher_count`   int(11)        DEFAULT NULL COMMENT '教职工人数',
    `school_description`     text           DEFAULT NULL COMMENT '学校简介',
    `school_features`        text           DEFAULT NULL COMMENT '学校特色',
    `school_logo_url`        varchar(255)   DEFAULT NULL COMMENT '学校logo',
    `school_photo_url`       varchar(255)   DEFAULT NULL COMMENT '学校照片',
    `school_is_985`          tinyint(1)     DEFAULT '0' COMMENT '是否985（0-否，1-是）',
    `school_is_211`          tinyint(1)     DEFAULT '0' COMMENT '是否211（0-否，1-是）',
    `school_is_double_first` tinyint(1)     DEFAULT '0' COMMENT '是否双一流（0-否，1-是）',
    `school_ranking`         int(11)        DEFAULT NULL COMMENT '学校排名',
    `school_status`          tinyint(1)     DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
    `school_sort`            int(11)        DEFAULT '0' COMMENT '排序',
    `school_remark`          varchar(500)   DEFAULT NULL COMMENT '备注',
    `school_deleted`         tinyint(1)     DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`            datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`            datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`              varchar(50)    DEFAULT NULL COMMENT '创建人',
    `update_by`              varchar(50)    DEFAULT NULL COMMENT '更新人',
    `create_name`            varchar(100)   DEFAULT NULL COMMENT '创建人姓名',
    `update_name`            varchar(100)   DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_id`),
    UNIQUE KEY `uk_school_code` (`school_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校信息表';

-- 学校同步配置表
CREATE TABLE `school_sync_config`
(
    `school_sync_config_id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_sync_config_name`           varchar(50)  DEFAULT NULL COMMENT '配置名称',
    `school_sync_config_cookie`         text         DEFAULT NULL COMMENT 'cookie',
    `school_sync_config_sync_type`      varchar(20)  DEFAULT NULL COMMENT '同步类型',
    `school_sync_config_sync_sync_type` varchar(20)  DEFAULT NULL COMMENT '同步类型',
    `school_sync_config_sync_status`    int(11)      DEFAULT NULL COMMENT '同步状态（0-未同步，1-同步中，2-同步成功，3-同步失败）',
    `school_sync_config_sync_time`      varchar(20)  DEFAULT NULL COMMENT '同步时间',
    `school_sync_config_enabled`        bit(1)       DEFAULT NULL COMMENT '是否启用',
    `school_sync_config_deleted`        tinyint(1)   DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                       datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                         varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                         varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                       varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                       varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_sync_config_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校同步配置表';

-- 学校专业表
CREATE TABLE `school_major`
(
    `school_major_id`                 bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_major_school_id`          bigint(20)   NOT NULL COMMENT '学校ID',
    `school_major_school_code`        varchar(50)  NOT NULL COMMENT '学校代码',
    `school_major_code`               varchar(50)  NOT NULL COMMENT '专业代码',
    `school_major_name`               varchar(100) NOT NULL COMMENT '专业名称',
    `school_major_category`           varchar(50)  DEFAULT NULL COMMENT '专业类别',
    `school_major_subject`            varchar(50)  DEFAULT NULL COMMENT '专业学科',
    `school_major_education_level`    varchar(20)  DEFAULT NULL COMMENT '学历层次（本科、专科）',
    `school_major_degree`             varchar(50)  DEFAULT NULL COMMENT '学位授予',
    `school_major_study_duration`     varchar(20)  DEFAULT NULL COMMENT '修学年限',
    `school_major_establish_year`     varchar(4)   DEFAULT NULL COMMENT '开设年份',
    `school_major_ranking`            int(11)      DEFAULT NULL COMMENT '专业排名',
    `school_major_is_featured`        tinyint(1)   DEFAULT '0' COMMENT '是否为特色专业（0-否，1-是）',
    `school_major_is_key_major`       tinyint(1)   DEFAULT '0' COMMENT '是否为重点专业（0-否，1-是）',
    `school_major_training_objective` text         DEFAULT NULL COMMENT '培养目标',
    `school_major_core_courses`       text         DEFAULT NULL COMMENT '核心课程',
    `school_major_career_prospects`   text         DEFAULT NULL COMMENT '就业方向',
    `school_major_detail_url`         varchar(255) DEFAULT NULL COMMENT '专业详情链接',
    `school_major_deleted`            tinyint(1)   DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                       varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                       varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                     varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                     varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_major_id`),
    KEY `idx_school_major_school_id` (`school_major_school_id`),
    KEY `idx_school_major_code` (`school_major_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校专业表';

-- 学校学生表
CREATE TABLE `school_student`
(
    `school_student_id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `school_student_school_id`     bigint(20)  NOT NULL COMMENT '学校ID',
    `school_student_school_code`   varchar(50) NOT NULL COMMENT '学校代码',
    `school_student_number`        varchar(50) NOT NULL COMMENT '学号',
    `school_student_name`          varchar(50) NOT NULL COMMENT '学生姓名',
    `school_student_gender`        tinyint(1)   DEFAULT NULL COMMENT '性别（0-女，1-男）',
    `school_student_birth_date`    date         DEFAULT NULL COMMENT '出生日期',
    `school_student_id_card`       varchar(18)  DEFAULT NULL COMMENT '身份证号',
    `school_student_phone`         varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `school_student_email`         varchar(100) DEFAULT NULL COMMENT '邮箱',
    `school_student_address`       varchar(255) DEFAULT NULL COMMENT '家庭住址',
    `school_student_grade_id`      bigint(20)   DEFAULT NULL COMMENT '年级ID',
    `school_student_grade_name`    varchar(50)  DEFAULT NULL COMMENT '年级名称',
    `school_student_class_id`      bigint(20)   DEFAULT NULL COMMENT '班级ID',
    `school_student_class_name`    varchar(50)  DEFAULT NULL COMMENT '班级名称',
    `school_student_enroll_date`   date         DEFAULT NULL COMMENT '入学日期',
    `school_student_graduate_date` date         DEFAULT NULL COMMENT '毕业日期',
    `school_student_status`        tinyint(1)   DEFAULT '1' COMMENT '学籍状态（0-休学，1-在读，2-毕业，3-退学）',
    `school_student_photo_url`     varchar(255) DEFAULT NULL COMMENT '照片URL',
    `school_student_parent_name`   varchar(50)  DEFAULT NULL COMMENT '家长姓名',
    `school_student_parent_phone`  varchar(20)  DEFAULT NULL COMMENT '家长联系电话',
    `school_student_remark`        varchar(500) DEFAULT NULL COMMENT '备注',
    `school_student_deleted`       tinyint(1)   DEFAULT '0' COMMENT '是否删除（0-未删除，1-已删除）',
    `create_time`                  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                  datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                    varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                    varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                  varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                  varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_student_id`),
    KEY `idx_school_student_school_id` (`school_student_school_id`),
    KEY `idx_school_student_number` (`school_student_number`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校学生表';

-- 历年分数表
CREATE TABLE `school_historical_score`
(
    `school_historical_score_id`              bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_historical_score_school_id`       bigint(20)  NOT NULL COMMENT '学校ID',
    `school_historical_score_school_code`     varchar(50) NOT NULL COMMENT '学校代码',
    `school_historical_score_major_id`        bigint(20)    DEFAULT NULL COMMENT '专业ID',
    `school_historical_score_major_code`      varchar(50)   DEFAULT NULL COMMENT '专业代码',
    `school_historical_score_year`            int(4)      NOT NULL COMMENT '年份',
    `school_historical_score_province`        varchar(50) NOT NULL COMMENT '省份',
    `school_historical_score_subject_type`    varchar(20)   DEFAULT NULL COMMENT '科目类型（理科、文科、综合）',
    `school_historical_score_batch`           varchar(50)   DEFAULT NULL COMMENT '批次（提前批、一批、二批等）',
    `school_historical_score_admission_type`  varchar(50)   DEFAULT NULL COMMENT '招生类型（普通类、国家专项等）',
    `school_historical_score_max_score`       int(11)       DEFAULT NULL COMMENT '最高分',
    `school_historical_score_min_score`       int(11)       DEFAULT NULL COMMENT '最低分',
    `school_historical_score_avg_score`       int(11)       DEFAULT NULL COMMENT '平均分',
    `school_historical_score_admission_count` int(11)       DEFAULT NULL COMMENT '录取人数',
    `school_historical_score_applied_count`   int(11)       DEFAULT NULL COMMENT '投档人数',
    `school_historical_score_provincial_line` int(11)       DEFAULT NULL COMMENT '省控线',
    `school_historical_score_lowest_rank`     int(11)       DEFAULT NULL COMMENT '最低位次',
    `school_historical_score_admission_rate`  decimal(5, 2) DEFAULT NULL COMMENT '录取率',
    `school_historical_score_remark`          varchar(500)  DEFAULT NULL COMMENT '备注',
    `school_historical_score_deleted`         tinyint(1)    DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                             datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                             datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                               varchar(50)   DEFAULT NULL COMMENT '创建人',
    `update_by`                               varchar(50)   DEFAULT NULL COMMENT '更新人',
    `create_name`                             varchar(100)  DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                             varchar(100)  DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_historical_score_id`),
    KEY `idx_school_historical_score_school_id` (`school_historical_score_school_id`),
    KEY `idx_school_historical_score_major_id` (`school_historical_score_major_id`),
    KEY `idx_school_historical_score_year` (`school_historical_score_year`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='历年分数表';

-- 招生计划表
CREATE TABLE `school_admission_plan`
(
    `school_admission_plan_id`                   bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_admission_plan_school_id`            bigint(20)  NOT NULL COMMENT '学校ID',
    `school_admission_plan_school_code`          varchar(50) NOT NULL COMMENT '学校代码',
    `school_admission_plan_major_id`             varchar(50)    DEFAULT NULL COMMENT '专业ID',
    `school_admission_plan_major_code`           varchar(50)    DEFAULT NULL COMMENT '专业代码',
    `school_admission_plan_year`                 int(4)      NOT NULL COMMENT '招生年份',
    `school_admission_plan_province`             varchar(50) NOT NULL COMMENT '招生省份',
    `school_admission_plan_subject_type`         varchar(20)    DEFAULT NULL COMMENT '科目类型（理科、文科、综合）',
    `school_admission_plan_plan_count`           int(11)        DEFAULT NULL COMMENT '计划招生人数',
    `school_admission_plan_actual_count`         int(11)        DEFAULT NULL COMMENT '实际招生人数',
    `school_admission_plan_tuition`              decimal(10, 2) DEFAULT NULL COMMENT '学费（元/年）',
    `school_admission_plan_batch`                varchar(50)    DEFAULT NULL COMMENT '招生批次（提前批、一批、二批等）',
    `school_admission_plan_admission_type`       varchar(50)    DEFAULT NULL COMMENT '招生类型（普通类、国家专项等）',
    `school_admission_plan_subject_requirements` text           DEFAULT NULL COMMENT '选科要求',
    `school_admission_plan_remark`               varchar(500)   DEFAULT NULL COMMENT '备注',
    `school_admission_plan_deleted`              tinyint(1)     DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                                datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                                datetime       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                                  varchar(50)    DEFAULT NULL COMMENT '创建人',
    `update_by`                                  varchar(50)    DEFAULT NULL COMMENT '更新人',
    `create_name`                                varchar(100)   DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                                varchar(100)   DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_admission_plan_id`),
    KEY `idx_school_admission_plan_school_id` (`school_admission_plan_school_id`),
    KEY `idx_school_admission_plan_year` (`school_admission_plan_year`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='招生计划表';

-- 学校课程表
CREATE TABLE `school_course`
(
    `school_course_id`           bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_course_school_id`    bigint(20)   NOT NULL COMMENT '学校ID',
    `school_course_school_code`  varchar(50)  NOT NULL COMMENT '学校代码',
    `school_course_code`         varchar(50)  NOT NULL COMMENT '课程编号',
    `school_course_name`         varchar(100) NOT NULL COMMENT '课程名称',
    `school_course_category`     varchar(50)   DEFAULT NULL COMMENT '课程类别',
    `school_course_type`         varchar(20)   DEFAULT NULL COMMENT '课程类型（必修、选修）',
    `school_course_credit`       decimal(5, 2) DEFAULT NULL COMMENT '学分',
    `school_course_hours`        int(11)       DEFAULT NULL COMMENT '课时',
    `school_course_desc`         text          DEFAULT NULL COMMENT '课程描述',
    `school_course_department`   varchar(100)  DEFAULT NULL COMMENT '开课部门',
    `school_course_grade_level`  varchar(50)   DEFAULT NULL COMMENT '适用年级',
    `school_course_teacher_id`   bigint(20)    DEFAULT NULL COMMENT '授课教师ID',
    `school_course_teacher_name` varchar(50)   DEFAULT NULL COMMENT '授课教师姓名',
    `school_course_syllabus`     text          DEFAULT NULL COMMENT '课程大纲',
    `school_course_textbook`     varchar(255)  DEFAULT NULL COMMENT '教材',
    `school_course_assessment`   varchar(50)   DEFAULT NULL COMMENT '考核方式',
    `school_course_status`       tinyint(1)    DEFAULT '1' COMMENT '课程状态（0-停用，1-启用）',
    `school_course_remark`       varchar(500)  DEFAULT NULL COMMENT '备注',
    `school_course_deleted`      tinyint(1)    DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                  varchar(50)   DEFAULT NULL COMMENT '创建人',
    `update_by`                  varchar(50)   DEFAULT NULL COMMENT '更新人',
    `create_name`                varchar(100)  DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                varchar(100)  DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_course_id`),
    KEY `idx_school_course_school_id` (`school_course_school_id`),
    KEY `idx_school_course_code` (`school_course_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校课程表';

-- 学校年级表
CREATE TABLE `school_grade`
(
    `school_grade_id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `school_grade_school_id`     bigint(20)  NOT NULL COMMENT '学校ID',
    `school_grade_school_code`   varchar(50) NOT NULL COMMENT '学校代码',
    `school_grade_name`          varchar(50) NOT NULL COMMENT '年级名称',
    `school_grade_code`          varchar(50) NOT NULL COMMENT '年级编码',
    `school_grade_phase`         tinyint(1)   DEFAULT NULL COMMENT '学制阶段（1-小学，2-初中，3-高中）',
    `school_grade_enroll_year`   int(4)       DEFAULT NULL COMMENT '入学年份',
    `school_grade_graduate_year` int(4)       DEFAULT NULL COMMENT '毕业年份',
    `school_grade_class_count`   int(11)      DEFAULT NULL COMMENT '班级数量',
    `school_grade_student_count` int(11)      DEFAULT NULL COMMENT '学生数量',
    `school_grade_director_id`   bigint(20)   DEFAULT NULL COMMENT '年级主任ID',
    `school_grade_director_name` varchar(50)  DEFAULT NULL COMMENT '年级主任姓名',
    `school_grade_status`        tinyint(1)   DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
    `school_grade_sort`          int(11)      DEFAULT '0' COMMENT '排序',
    `school_grade_remark`        varchar(500) DEFAULT NULL COMMENT '备注',
    `school_grade_deleted`       tinyint(1)   DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                  varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                  varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_grade_id`),
    KEY `idx_school_grade_school_id` (`school_grade_school_id`),
    KEY `idx_school_grade_code` (`school_grade_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校年级表';

-- 学校班级表
CREATE TABLE `school_class`
(
    `school_class_id`            bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `school_class_school_id`     bigint(20)  NOT NULL COMMENT '学校ID',
    `school_class_school_code`   varchar(50) NOT NULL COMMENT '学校代码',
    `school_class_grade_id`      bigint(20)  NOT NULL COMMENT '年级ID',
    `school_class_name`          varchar(50) NOT NULL COMMENT '班级名称',
    `school_class_code`          varchar(50) NOT NULL COMMENT '班级编号',
    `school_class_capacity`      int(11)      DEFAULT NULL COMMENT '班级容量',
    `school_class_current_count` int(11)      DEFAULT NULL COMMENT '当前人数',
    `school_class_teacher_id`    bigint(20)   DEFAULT NULL COMMENT '班主任ID',
    `school_class_teacher_name`  varchar(50)  DEFAULT NULL COMMENT '班主任姓名',
    `school_class_description`   varchar(500) DEFAULT NULL COMMENT '班级描述',
    `school_class_status`        tinyint(1)   DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
    `school_class_sort`          int(11)      DEFAULT '0' COMMENT '排序',
    `school_class_remark`        varchar(500) DEFAULT NULL COMMENT '备注',
    `school_class_student_count` int(11)      DEFAULT NULL COMMENT '学生人数',
    `school_class_room_id`       bigint(20)   DEFAULT NULL COMMENT '教室ID',
    `school_class_room_name`     varchar(50)  DEFAULT NULL COMMENT '教室名称',
    `school_class_deleted`       tinyint(1)   DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                  varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                  varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_class_id`),
    KEY `idx_school_class_school_id` (`school_class_school_id`),
    KEY `idx_school_class_grade_id` (`school_class_grade_id`),
    KEY `idx_school_class_code` (`school_class_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校班级表';

-- 学校教师表
CREATE TABLE `school_teacher`
(
    `school_teacher_id`                bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `school_teacher_school_id`         bigint(20)  NOT NULL COMMENT '学校ID',
    `school_teacher_school_code`       varchar(50) NOT NULL COMMENT '学校代码',
    `school_teacher_number`            varchar(50) NOT NULL COMMENT '教师工号',
    `school_teacher_name`              varchar(50) NOT NULL COMMENT '教师姓名',
    `school_teacher_gender`            tinyint(1)   DEFAULT NULL COMMENT '性别（0-女，1-男）',
    `school_teacher_birth_date`        date         DEFAULT NULL COMMENT '出生日期',
    `school_teacher_id_card`           varchar(18)  DEFAULT NULL COMMENT '身份证号',
    `school_teacher_phone`             varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `school_teacher_email`             varchar(100) DEFAULT NULL COMMENT '邮箱',
    `school_teacher_address`           varchar(255) DEFAULT NULL COMMENT '住址',
    `school_teacher_department`        varchar(100) DEFAULT NULL COMMENT '所属部门',
    `school_teacher_position`          varchar(50)  DEFAULT NULL COMMENT '职位',
    `school_teacher_title`             varchar(50)  DEFAULT NULL COMMENT '职称',
    `school_teacher_subject`           varchar(50)  DEFAULT NULL COMMENT '任教科目',
    `school_teacher_entry_date`        date         DEFAULT NULL COMMENT '入职日期',
    `school_teacher_leave_date`        date         DEFAULT NULL COMMENT '离职日期',
    `school_teacher_status`            tinyint(1)   DEFAULT '1' COMMENT '状态（0-离职，1-在职，2-休假）',
    `school_teacher_photo_url`         varchar(255) DEFAULT NULL COMMENT '照片URL',
    `school_teacher_education`         varchar(50)  DEFAULT NULL COMMENT '学历',
    `school_teacher_degree`            varchar(50)  DEFAULT NULL COMMENT '学位',
    `school_teacher_graduation_school` varchar(100) DEFAULT NULL COMMENT '毕业院校',
    `school_teacher_major`             varchar(100) DEFAULT NULL COMMENT '专业',
    `school_teacher_remark`            varchar(500) DEFAULT NULL COMMENT '备注',
    `school_teacher_deleted`           tinyint(1)   DEFAULT '0' COMMENT '删除标志（0-存在，1-删除）',
    `create_time`                      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`                      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`                        varchar(50)  DEFAULT NULL COMMENT '创建人',
    `update_by`                        varchar(50)  DEFAULT NULL COMMENT '更新人',
    `create_name`                      varchar(100) DEFAULT NULL COMMENT '创建人姓名',
    `update_name`                      varchar(100) DEFAULT NULL COMMENT '更新人姓名',
    PRIMARY KEY (`school_teacher_id`),
    KEY `idx_school_teacher_school_id` (`school_teacher_school_id`),
    KEY `idx_school_teacher_number` (`school_teacher_number`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学校教师表';