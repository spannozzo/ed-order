create table if not exists order_table (
       id varchar(255) not null,
        status varchar(255),
        totalAmount float8,
        primary key (id)
    );
    
insert into order_table (id,status, totalAmount) values ('3777616f-a80e-4b24-a20f-55e947a81b07', 'CREATED', '58.90');
insert into order_table (id,status, totalAmount) values ('3777616f-a80e-4b24-a20f-55e947a81b08', 'SHIPPED', '99.90');
insert into order_table (id,status, totalAmount) values ('3777616f-a80e-4b24-a20f-55e947a81b09', 'CREATED', '15.45');