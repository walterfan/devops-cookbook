CREATE TABLE link (
id INT PRIMARY KEY auto_increment, 
name varchar(16) , 
url varchar(128) ,tags varchar(128)) ;


CREATE TABLE category (
id INT PRIMARY KEY  auto_increment, 
name varchar(16) NOT NULL) ;


CREATE TABLE linkcategory (
linkid INT NOT NULL, 
categoryid INT NOT NULL, 
PRIMARY KEY(linkid, categoryid) ) ;

create table account (
accountID INT PRIMARY KEY  auto_increment,
userName varchar(32),
password varchar(32),
siteName varchar(32),
siteUrl varchar(128),
email varchar(64),
isEncrypted boolean
);