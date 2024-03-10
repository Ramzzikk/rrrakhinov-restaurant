--liquibase formatted sql
--kakao:13

create table users
(
    id       uuid not null
        primary key,
    username varchar(255),
    password varchar(255),
    type     varchar(255)
);

alter table users
    owner to postgres;

create table menu
(
    id              serial not null
        primary key,
    name            varchar(255),
    description     varchar(255),
    minutes_to_cook integer not null,
    price           numeric(38, 2),
    author          varchar(255)
);

alter table menu
    owner to postgres;

create table orderings
(
    id           serial not null
        primary key,
    price        numeric(38, 2),
    time_started time(6),
    time_ended   time(6),
    customer     varchar(255)
);

alter table orderings
    owner to postgres;

create table ordering_dishid
(
    ordering_id integer not null
        constraint fkh78ymb99v8xsdlct0p7lh60ig
            references orderings,
    dishid      integer
);

alter table ordering_dishid
    owner to postgres;

create table feedbacks
(
    id           serial not null
        primary key,
    order_id        integer,
    assessment      integer,
    text     varchar(255)
);

alter table feedbacks
    owner to postgres;