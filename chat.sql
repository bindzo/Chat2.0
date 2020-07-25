CREATE DATABASE chat;
use chat;
create table nguoidung (
tendangnhap varchar(20) not null,
matkhau varchar(50) not null,
primary key(tendangnhap)
);
select * from nguoidung;
INSERT INTO nguoidung(tendangnhap, matkhau) VALUES ('a', '1');
INSERT INTO nguoidung(tendangnhap, matkhau) VALUES ('b', '2');
INSERT INTO nguoidung(tendangnhap, matkhau) VALUES ('c', '3');