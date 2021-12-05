# Install wordpress on ubuntu
* refer to https://www.journaldev.com/25670/install-wordpress-nginx-ubuntu

## Nginx
```
apt-get update
apt-get upgrade
apt-get install nginx
```
## MySQL

```
# apt-get install mariadb-server 
# systemctl enable mariadb.service
# mysql_secure_installation
```

## PHP

```

# apt-get install php7.2 php7.2-cli php7.2-fpm php7.2-mysql php7.2-json php7.2-opcache php7.2-mbstring php7.2-xml php7.2-gd php7.2-curl
```

## create wordpress db

```
$ mysql -u root -p
Enter password:

MariaDB [mysql]> CREATE DATABASE wordpress_db;
Query OK, 1 row affected (0.00 sec)

MariaDB [mysql]> GRANT ALL ON wordpress_db.* TO 'wpuser'@'localhost' IDENTIFIED BY 'Passw0rd!' WITH GRANT OPTION;
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> exit
```