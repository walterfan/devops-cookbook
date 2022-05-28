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

MariaDB [mysql]> GRANT ALL ON wordpress_db.* TO 'wpuser'@'localhost' IDENTIFIED BY 'P@ss1234' WITH GRANT OPTION;
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> FLUSH PRIVILEGES;
Query OK, 0 rows affected (0.00 sec)

MariaDB [mysql]> exit
```

## Nginx configuration

```
# cd /etc/nginx/sites-available
# cat wordpress.conf
server {
            listen 80;
            root /var/www/html/wordpress/public_html;
            index index.php index.html;
            server_name SUBDOMAIN.DOMAIN.TLD;

	         access_log /var/log/nginx/SUBDOMAIN.access.log;
    	     error_log /var/log/nginx/SUBDOMAIN.error.log;

            location / {
                         try_files $uri $uri/ =404;
            }

            location ~ \.php$ {
                         include snippets/fastcgi-php.conf;
                         fastcgi_pass unix:/run/php/php7.2-fpm.sock;
            }
            
            location ~ /\.ht {
                         deny all;
            }

            location = /favicon.ico {
                         log_not_found off;
                         access_log off;
            }

            location = /robots.txt {
                         allow all;
                         log_not_found off;
                         access_log off;
           }
       
            location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
                         expires max;
                         log_not_found off;
           }
}

```


## Install wordpress

```
# wget https://wordpress.org/latest.tar.gz
# tar -zxvf latest.tar.gz
# cd wordpress
# cp wp-config-sample.php wp-config.php

# vi wp-config.php
-----------------------------------------
define( 'DB_NAME', 'wordpress_db' );

/** MySQL database username */
define( 'DB_USER', 'wpuser' );

/** MySQL database password */
define( 'DB_PASSWORD', 'P@ss1234' );

/** MySQL hostname */
define( 'DB_HOST', 'localhost' );


```