create table user
(
    username     varchar(20)                  null comment '用户名',
    id           bigint unsigned auto_increment comment '主键 自增'
        primary key,
    gender       tinyint unsigned             null comment '性别 1 男 2 女',
    phone        char(11)                     null comment '手机号',
    password     varchar(255)                 not null comment '密码',
    avatar       varchar(255)                 null comment '头像的url',
    create_time  datetime                     null comment '创建时间',
    update_time  datetime                     null comment '修改时间',
    is_delete    tinyint          default 0   null comment '是否删除 0 没有被删除 1 被删除了',
    user_status  tinyint unsigned default '1' null comment '是否有效 1 正常',
    email        varchar(255)                 null comment '邮箱',
    user_account varchar(255)                 null comment '用户登录账号',
    role         tinyint          default 0   not null comment '用户角色 0 普通用户 1 管理员'
)
    comment '用户表';


