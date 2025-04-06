SET NAMES utf8mb4;

drop database if exists hfdy_pan;
create database hfdy_pan;
use hfdy_pan;

drop table if exists user;
create table user
(
    id              varchar(50) primary key not null comment 'ID',
    nick_name       varchar(20)  default null comment '昵称',
    is_admin        tinyint(1)   default 0  not null comment '是否是管理员',
    email           varchar(200) default null comment '邮箱',
    qq_open_id      varchar(50)  default null comment '第三方登录id',
    avatar          varchar(100) default null comment '头像在minio中的路径',
    password        varchar(50)  default null comment 'MD5之后的密码',
    register_time   datetime     default null comment '注册时间',
    last_login_time datetime     default null comment '上次登录时间',
    status          tinyint(1)   default null comment '用户状态(0禁用 1启用)',
    is_vip          tinyint(1)   default 0 comment '是不是vip',
    used_space      bigint(50)   default null comment '已使用云盘空间 byte',
    total_space     bigint(50)   default null comment '总云盘空间 byte',
    upload_limit    smallint     default 6 comment '默认同时上传数量',
    download_speed  float        default 2048000 comment '默认下载速度限制，2MB/s'
) comment '用户信息表';
create index key_qq_open_id on user (qq_open_id);
create unique index user_email on user (email);

-- #默认密码123456a
INSERT INTO `hfdy_pan`.`user` (`id`, `nick_name`, `is_admin`, `email`, `qq_open_id`, `avatar`, `password`,
                               `register_time`, `last_login_time`, `status`, `is_vip`, `used_space`, `total_space`,
                               `upload_limit`, `download_speed`)
VALUES ('1', '普通用户', 0, 'email1@qq.com', '', '', '9cbf8a4dcb8e30682b927f352d6559a0', '2025-01-17 23:00:30',
        '2025-01-18 19:27:08', 1, 0, 0, 1073741824, 6, 2097512),
       ('1880268966581248002', '会飞的鱼', 1, 'email2@qq.com', '',
        '', '9cbf8a4dcb8e30682b927f352d6559a0', '2025-01-17 23:00:30',
        '2025-03-26 20:41:39', 1, 1, 8696042, 1073741824, 6, 10485760);



drop table if exists file;
create table file
(
    id          varchar(50) primary key not null comment 'ID',
    user_id     varchar(50)             not null comment '文件所属用户id',
    md5         varchar(32)             not null comment '文件md5用于秒传',
    level       varchar(300)            not null comment '文件层级，例如：1-1-2',
    name        varchar(100)            not null comment '文件名',
    path        varchar(200)            not null comment 'minio中的路径',
    create_time datetime                         default current_timestamp() comment '创建时间',
    update_time datetime                         default current_timestamp() comment '更新时间',
    delete_time datetime                         default null comment '删除时间',
    size        bigint unsigned                  default null comment '文件大小，单位是B，文件夹没有',
    pid         varchar(50)                      default '' comment '父级文件夹id，可为空',
    category    varchar(20)             not null comment '文件分类，包括文件夹，用于前端显示选项',
    media_type  varchar(10)             not null comment '文件具体类型',
    is_deleted  tinyint(1)              not null comment '逻辑删除，0未删除，1删除进了回收站',
    status      tinyint(1)              not null default 0 comment '状态，0未转码或无需转码，1转码中，2转码成功，3转码失败',
    lyric_path  varchar(200)            not null default '' comment '歌词路径，音乐独有'
) comment '文件表';
create index idx_user_id on file (user_id);
create index idx_pid on file (pid);


drop table if exists share_file;
create table share_file
(
    share_id varchar(50) not null comment 'ID',
    file_id  varchar(50) not null comment 'ID'
) comment '分享-文件关联表';


drop table if exists share;
create table share
(
    id          varchar(50) primary key not null comment 'ID',
    user_id     varchar(50)             not null comment '用户id',
    expire      bigint                  not null default 0 comment '有效时长，0表示永久',
    code        varchar(32)             not null default '' comment '提取码，空字符串表示公开',
    create_time datetime                         default current_timestamp() comment '创建时间',
    visit_num   int                     not null default 0 comment '访问量'
) comment '分享表';
create index idx_user_id on share (user_id);