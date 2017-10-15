# msa
Micro Service in Action

# Examples
## Account

## Guestbook

## Checklist


# Docker

## build LEMP
* Linux
* Nginx
* MySQL
* PHP

### Prepare

* mkdir -p logs
* mdkir -p data/db/mysql
* mkdir -p web/public

### start
```
docker-compose up -d
```
### stop
```
docker-compose down -v
```

### commands

docker-compose rm -v

docker-compose exec mysqldb /bin/bash
	mysql -u root -p

open localhost:1980 by browser

```
  * server:mysql
  * username:root
  * password: pass1234
```