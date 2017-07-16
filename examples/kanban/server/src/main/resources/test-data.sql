INSERT INTO link (id, name, url, tags) 
VALUES (1, 'weibo', 'http://weibo.com/fanyamin', 'sns');

INSERT INTO link (id, name, url, tags) 
VALUES (2, 'webqq', 'http://web2.qq.com', 'sns');

INSERT INTO category (id, name) 
VALUES (1, 'site');

INSERT INTO linkcategory (linkid, categoryid) 
VALUES (1, 1);

INSERT INTO linkcategory (linkid, categoryid) 
VALUES (2, 1);

insert into account(accountID, userName, password ,siteName, siteUrl,email)
values (1, 'walter', 'pass','weibo.com','https://weibo.com/fanyamin');