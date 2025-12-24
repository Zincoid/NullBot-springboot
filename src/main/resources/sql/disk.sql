# CREATE DATABASE NullBot;
# USE NullBot;

drop table if exists file;

create table file
(
    id int auto_increment primary key,
    file_name varchar(255) not null,
    file_size bigint not null,
    directory varchar(255) not null,
    is_dir int default 0 not null
);
