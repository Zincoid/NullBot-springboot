# CREATE DATABASE NullBot;
# USE NullBot;

drop table if exists file;
drop table if exists admin;

create table file
(
    id int auto_increment primary key,
    file_name varchar(255) not null,
    file_size bigint not null,
    directory varchar(255) not null,
    is_dir int default 0 not null
);

create table admin
(
    id bigint auto_increment primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    email varchar(255) not null
);