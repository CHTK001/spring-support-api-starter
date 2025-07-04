create table sys_dept
(
    sys_dept_id                      int auto_increment
        primary key,
    sys_dept_name                    varchar(255) null comment '机构名称',
    sys_dept_pid                     int          null comment '父机构',
    sys_dept_tree_id                 varchar(255) null comment '机构链路',
    sys_dept_code                    varchar(255) null comment '组织编码',
    sys_dept_principal               varchar(255) null comment '部门负责人',
    sys_dept_contact                 varchar(255) null comment '负责人联系方式',
    sys_dept_sort                    int          null comment '优先级',
    sys_dept_status                  int          null comment '状态; 是否禁用; 0:启用',
    sys_dept_remark                  varchar(255) null comment '备注',
    sys_dept_data_permission         int          null comment '数据权限类型;1:全部可见;2:本人可见;3:所在部门可见;4:所在部门及子级可见;5:选择的部门可见',
    sys_dept_data_permission_dept_id varchar(255) null comment '数据权限可见的部门ID列表',
    create_name                      varchar(255) null comment '创建人姓名',
    create_by                        int          null comment '创建人',
    create_time                      datetime     null comment '创建时间',
    update_time                      datetime     null comment '更新时间',
    update_name                      varchar(255) null comment '更新人姓名',
    update_by                        int          null comment '更新人'
)
    comment '组织机构' row_format = DYNAMIC;

create table sys_role
(
    sys_role_id         int auto_increment
        primary key,
    sys_role_name       varchar(255)  null comment '角色名称',
    sys_role_code       varchar(255)  null comment '角色编码; 唯一',
    sys_role_in_system  int           null comment '是否系统角色; 0：非系统角色',
    sys_role_status     int           null comment '是否启用; 0：不启用',
    sys_role_sort       int           null comment '优先级',
    sys_role_remark     varchar(255)  null comment '备注',
    sys_role_hidden     int default 0 null comment '是否显示; 0:显示',
    sys_role_readable   int           null comment '角色可读',
    sys_role_writeable  int           null comment '角色可写',
    sys_role_executable int           null comment '角色可执行',
    sys_role_board_card int           null comment '角色可看板类型',
    create_name         varchar(255)  null comment '创建人姓名',
    create_by           int           null comment '创建人',
    create_time         datetime      null comment '创建时间',
    update_time         datetime      null comment '更新时间',
    update_name         varchar(255)  null comment '更新人姓名',
    update_by           int           null comment '更新人'
)
    comment '角色表' row_format = DYNAMIC;

create table sys_user
(
    sys_user_id               int auto_increment
        primary key,
    sys_user_username         varchar(255) null comment '账号',
    sys_user_nickname         varchar(255) null comment '名称',
    sys_user_password         varchar(255) null comment '密码',
    sys_user_salt             varchar(255) null comment '盐',
    sys_user_delete           int          null comment '是否删除; 0: 未删除',
    sys_user_status           int          null comment '是否启用；0：禁用',
    sys_user_phone            varchar(255) null comment '手机号',
    sys_user_email            varchar(255) null comment '邮箱',
    sys_user_sex              int          null comment '性别; 0:女;1:男; 2:其他',
    sys_user_full_address     varchar(400) null comment '居住地址',
    sys_user_in_system        int          null comment '是否系统账号; 0: 非系统账号',
    sys_user_avatar           text         null comment '头像',
    sys_user_card             varchar(255) null comment '身份证',
    sys_dept_id               int          null comment '部门ID',
    sys_user_remark           varchar(255) null comment '备注',
    sys_user_last_address     varchar(255) null comment '最后一次登录地点',
    sys_user_register_address varchar(255) null comment '注册地点',
    sys_user_last_ip          varchar(255) null comment '最后一次登录IP',
    sys_user_register_ip      varchar(255) null comment '注册地址',
    sys_user_login_count      int          null comment '登录次数',
    sys_client_id             varchar(255) null comment '客户端ID',
    sys_user_totp_code        varchar(600) null comment 'topt',
    create_name               varchar(255) null comment '创建人姓名',
    create_by                 int          null comment '创建人',
    create_time               datetime     null comment '创建时间',
    update_time               datetime     null comment '更新时间',
    update_name               varchar(255) null comment '更新人姓名',
    update_by                 int          null comment '更新人'
)
    comment '用户表' row_format = DYNAMIC;

create table sys_user_third_party
(
    sys_user_third_party_id    int auto_increment
        primary key,
    sys_user_third_party_type  varchar(255) null comment '三方类型. gitee',
    sys_user_third_party_name  varchar(255) null comment '三方信息名称',
    sys_user_third_party_value varchar(255) null comment '三方信息',
    sys_user_id                int          null comment '当前系统用户ID',
    sys_user_third_party_uid   varchar(255) null comment '三方用户ID',
    create_name                varchar(255) null comment '创建人姓名',
    create_by                  int          null comment '创建人',
    create_time                datetime     null comment '创建时间',
    update_time                datetime     null comment '更新时间',
    update_name                varchar(255) null comment '更新人姓名',
    update_by                  int          null comment '更新人'
)
    comment '用户三方绑定表' row_format = DYNAMIC;

