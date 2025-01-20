create
    database if not exists hfdy_pan;
use
    hfdy_pan;

drop table if exists user;
create table user
(
    id              varchar(50) primary key not null comment 'ID',
    nick_name       varchar(20)  default null comment '昵称',
    is_admin        tinyint(1)   default 0  not null comment '是否是管理员',
    email           varchar(200) default null comment '邮箱',
    qq_open_id      varchar(50)  default null comment '第三方登录id',
    avatar          varchar(500) default null comment '头像在minio中的路径',
    password        varchar(50)  default null comment 'MD5之后的密码',
    register_time   datetime     default null comment '注册时间',
    last_login_time datetime     default null comment '上次登录时间',
    status          tinyint(1)   default null comment '用户状态(0禁用 1启用)',
    used_space      bigint(50)   default null comment '已使用云盘空间 byte',
    total_space     bigint(50)   default null comment '总云盘空间 byte'
) comment '用户信息表';
create unique index key_qq_open_id on user (qq_open_id);
create unique index user_email on user (email);

# 默认密码1952620883zzw@
INSERT INTO `hfdy_pan`.`user` (`id`, `nick_name`, `is_admin`, `email`, `qq_open_id`, `avatar`, `password`,
                               `register_time`, `last_login_time`, `status`, `used_space`, `total_space`)
VALUES ('1880268966581248002', '会飞的鱼', 0, '1952620883@qq.com', '', '/api/static/avatar.png',
        '1092ab66d3c8594489d0649b290813c0', '2025-01-17 23:00:30', '2025-01-18 19:27:08', 1, 0, 1073741824);



drop table if exists file;
create table file
(
    id          varchar(50) primary key not null comment 'ID',
    user_id     varchar(50)             not null comment '文件所属用户id',
    md5         varchar(32)             not null comment '文件md5用于秒传',
    name        varchar(100)            not null comment '文件名',
    path        varchar(200)            not null comment 'minio中的路径',
    create_time datetime    default current_timestamp() comment '创建时间',
    update_time datetime    default current_timestamp() comment '更新时间',
    delete_time datetime    default null comment '删除时间',
    size        int(32)     default null comment '文件大小，单位是B，文件夹没有',
    pid         varchar(50) default null comment '父级文件夹id，可为空',
    category    varchar(20)             not null comment '文件分类',
    media_type  varchar(10)             not null comment '文件具体类型',
    status      tinyint(1)              not null comment '状态，0转码中，1转码失败，2转码成功',
    is_deleted  tinyint(1)              not null comment '逻辑删除，0未删除，1删除进了回收站'
) comment '文件表';
create index idx_user_id on file (user_id);
create index idx_pid on file (pid);

INSERT INTO file (id, user_id, md5, name, path, create_time, update_time, delete_time, size, pid, category, media_type,
                  status, is_deleted)
VALUES
-- Video files
('1', '1880268966581248002', 'e4d909c29bacb7f9', '家庭聚会录像.mp4', '/sgeslab/11_家庭聚会录像.mp4',
 '2025-01-01 10:00:00', '2025-01-01 10:00:00', NULL, 3212334, NULL, 'video', 'video', 2, 0),
('2', '1880268966581248002', 'aaf4c61ddcc5e8a2', '旅行纪录片.avi', '/sgeslab/2_旅行纪录片.avi',
 '2025-01-02 11:00:00', '2025-01-02 11:00:00', NULL, 1234321, NULL, 'video', 'video', 2, 0),
('3', '1880268966581248002', 'c3fcd3d84131efeb', '儿童节表演.mkv', '/sgeslab/3_儿童节表演.mkv',
 '2025-01-03 12:00:00', '2025-01-03 12:00:00', NULL, 543, NULL, 'video', 'video', 2, 0),
('4', '1880268966581248002', '25d55ad283aa400af', '毕业典礼视频.mp4', '/sgeslab/4_毕业典礼视频.mp4',
 '2025-01-04 13:00:00', '2025-01-04 13:00:00', NULL, 23454, NULL, 'video', 'video', 2, 0),
('5', '1880268966581248002', '5f4dcc3b5aa765d6', '婚礼录像.avi', '/sgeslab/5_婚礼录像.avi',
 '2025-01-05 14:00:00', '2025-01-05 14:00:00', NULL, 1234, NULL, 'video', 'video', 2, 0),

-- Audio files
('6', '1880268966581248002', '92eb5ffee7dae2ce', '清晨音乐.mp3', '/sgeslab/6_清晨音乐.mp3',
 '2025-01-06 15:00:00', '2025-01-06 15:00:00', NULL, 2345, NULL, 'audio', 'audio', 2, 0),
('7', '1880268966581248002', '7c6a180b34286ed2', '午夜爵士.flac', '/sgeslab/7_午夜爵士.flac',
 '2025-01-07 16:00:00', '2025-01-07 16:00:00', NULL, 6543, NULL, 'audio', 'audio', 2, 0),
('8', '1880268966581248002', '4e07408562bedb8b', '音乐会实况.wav', '/sgeslab/8_音乐会实况.wav',
 '2025-01-08 17:00:00', '2025-01-08 17:00:00', NULL, 65432, NULL, 'audio', 'audio', 2, 0),
('9', '1880268966581248002', 'd4735e3a2d7d9e8f', '流行歌曲合集.mp3', '/sgeslab/9_流行歌曲合集.mp3',
 '2025-01-09 18:00:00', '2025-01-09 18:00:00', NULL, 6543, NULL, 'audio', 'audio', 2, 0),
('10', '1880268966581248002', '0cc175b9c0f1b6a8', '古典音乐欣赏.flac', '/sgeslab/10_古典音乐欣赏.flac',
 '2025-01-10 19:00:00', '2025-01-10 19:00:00', NULL, 101234567, NULL, 'audio', 'audio', 2, 0),

-- Image files
('11', '1880268966581248002', '9e107d9d372bb6dd', '夕阳美景.jpg', '/sgeslab/11_夕阳美景.jpg',
 '2025-01-11 20:00:00', '2025-01-11 20:00:00', NULL, 234, NULL, 'image', 'image', 2, 0),
('12', '1880268966581248002', 'b6ed26f3d5c4f3e5', '雪山风光.png', '/sgeslab/12_雪山风光.png',
 '2025-01-12 21:00:00', '2025-01-12 21:00:00', NULL, 123456, NULL, 'image', 'image', 2, 0),
('13', '1880268966581248002', 'f5d8eeec50da6d0b', '城市夜景.gif', '/sgeslab/13_城市夜景.gif',
 '2025-01-13 22:00:00', '2025-01-13 22:00:00', NULL, 23456543, NULL, 'image', 'image', 2, 0),
('14', '1880268966581248002', '65e84be33833fc47', '花卉特写.jpg', '/sgeslab/14_花卉特写.jpg',
 '2025-01-14 23:00:00', '2025-01-14 23:00:00', NULL, 123456, NULL, 'image', 'image', 2, 0),
('15', '1880268966581248002', '77de68daecd823bab', '星空摄影.png', '/sgeslab/15_星空摄影.png',
 '2025-01-15 00:00:00', '2025-01-15 00:00:00', NULL, 12345, NULL, 'image', 'image', 2, 0),

-- Document files
('16', '1880268966581248002', 'e4d909c29bacb7f9', '项目报告.docx', '/sgeslab/16_项目报告.docx',
 '2025-01-16 01:00:00', '2025-01-16 01:00:00', NULL, 234, NULL, 'docs', 'docx', 2, 0),
('17', '1880268966581248002', 'aaf4c61ddcc5e8a2', '财务分析.pdf', '/sgeslab/17_财务分析.pdf',
 '2025-01-17 02:00:00', '2025-01-17 02:00:00', NULL, 543, NULL, 'docs', 'pdf', 2, 0),
('18', '1880268966581248002', 'c3fcd3d84131efeb', '销售数据.xlsx', '/sgeslab/18_销售数据.xlsx',
 '2025-01-18 03:00:00', '2025-01-18 03:00:00', NULL, 87654, NULL, 'docs', 'xlsx', 2, 0),
('19', '1880268966581248002', '25d55ad283aa400af', '年度总结.pptx', '/sgeslab/19_年度总结.pptx',
 '2025-01-19 04:00:00', '2025-01-19 04:00:00', NULL, 876543, NULL, 'docs', 'pptx', 2, 0),
('20', '1880268966581248002', '5f4dcc3b5aa765d6', '会议纪要.md', '/sgeslab/20_会议纪要.md',
 '2025-01-20 05:00:00', '2025-01-20 05:00:00', NULL, 123456, NULL, 'docs', 'md', 2, 0),

-- Other files
('21', '1880268966581248002', '92eb5ffee7dae2ce', '个人资料备份.zip', '/sgeslab/21_个人资料备份.zip',
 '2025-01-21 06:00:00', '2025-01-21 06:00:00', NULL, 123456, NULL, 'others', 'zip', 2, 0),
('22', '1880268966581248002', '7c6a180b34286ed2', '旧照片.rar', '/sgeslab/22_旧照片.rar',
 '2025-01-22 07:00:00', '2025-01-22 07:00:00', NULL, 543, NULL, 'others', 'zip', 2, 0),
('23', '1880268966581248002', '4e07408562bedb8b', '笔记与想法.md', '/sgeslab/23_笔记与想法.md',
 '2025-01-23 08:00:00', '2025-01-23 08:00:00', NULL, 2345, NULL, 'docs', 'md', 2, 0),
('24', '1880268966581248002', 'd4735e3a2d7d9e8f', '代码库.zip', '/sgeslab/24_代码库.zip',
 '2025-01-24 09:00:00', '2025-01-24 09:00:00', NULL, 34567, NULL, 'others', 'zip', 2, 0),
('25', '1880268966581248002', '0cc175b9c0f1b6a8', '未知文件.ext', '/sgeslab/25_未知文件.ext',
 '2025-01-25 10:00:00', '2025-01-25 10:00:00', NULL, 234567, NULL, 'others', 'unknown', 2, 0),
('26', '1880268966581248002', 'd4735e3a2d7d9e8f', '我的文档', '/sgeslab/26_我的文档',
 '2025-01-26 10:00:00', '2025-01-26 10:00:00', NULL, NULL, NULL, 'others', 'folder', 2, 0);
