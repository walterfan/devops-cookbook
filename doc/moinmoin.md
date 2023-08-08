# Installation


## install python and virtualenv



## install moin
```
wget http://static.moinmo.in/files/moin-1.9.11.tar.gz
sha256sum moin-1.9.11.tar.gz

tar xvzf moin-1.9.11.tar.gz

cd moin-1.9.11
python wikiserver.py
```
## install uwsgi

```
wget https://projects.unbit.it/downloads/uwsgi-latest.tar.gz
tar zxvf uwsgi-latest.tar.gz
cd /opt/uwsgi-2.0.20
make
ln -s /opt/uwsgi-2.0.20/uwsgi /usr/local/bin/uwsgi
```

# Configuration




vi wikiserverconfig.py

host='172.31.208.68'


vi wikiconfig.py


acl_rights_default = u"walter:read,write,delete,revert,admin"

data_dir = '/Users/yafan/Documents/workspace/walter/wfnote/moinmoin'

copy text_markdown.py ./moinmoin/plugin/parser/text_markdown.py

create uwsgi.ini

```

[uwsgi]
socket = /var/run/moin.sock
chmod-socket = 660

chdir = /usr/share/moin/server
wsgi-file = moin.wsgi

master
workers = 3
max-requests = 200
harakiri = 30
die-on-term
```

* change configuration of wiki

```
sudo cp config/wikiconfig.py wikiconfig.py

sudo sed -i 's/Untitled Wiki/Walter Personal Wiki/' wikiconfig.py

sudo sed -i '/#superuser/a\    superuser = [u\"admin\", ]' wikiconfig.py

sudo sed -i '$a\    log_reverse_dns_lookups = False' wikiconfig.py
```

# Run moinmoin

```
source venv/bin/activate
python wikiserver.py &
```

# Run moinmoin by uWSgi
## 1) create two folders 

```
mkdir -p /etc/moin/log
mkdir -p /var/log/moin
chown moin /var/log/moin
```


## 2) create two files

```
$ touch /etc/moin/run
---------------------------
#!/bin/sh
cd /var/moin || exit
exec 2>&1 /usr/local/bin/setuidgid moin /usr/local/bin/uwsgi uwsgi.ini


$ touch /etc/moin/log/run
----------------------------------
#!/bin/sh
exec /usr/local/bin/setuidgid moin \
        /usr/local/bin/multilog t s300000 /var/log/moin
```

## 3) make them both executable, and then create a symbolic link to start the uWSGI service

```
chmod 755 /etc/moin/run /etc/moin/log/run
ln -s /etc/moin /service/moin
```

## 4) start uWSGI

```
echo 'su moin -c "cd /var/moin && /usr/local/bin/uwsgi uwsgi.ini" &' >> /etc/rc.local

or


cat <<EOF | sudo tee -a /etc/systemd/system/uwsgi.service
[Unit]
Description=uWSGI instance to serve MoinMoin
After=syslog.target


[Service]
ExecStart=/usr/bin/uwsgi --ini /usr/share/moin/uwsgi.ini
RuntimeDirectory=uwsgi
Restart=always
KillSignal=SIGQUIT
Type=notify
StandardError=syslog
NotifyAccess=all


[Install]
WantedBy=multi-user.target
EOF

```



# Change configuration of nginx

## change config of nginx

```
location / {
    uwsgi_pass unix:///run/moin/moin.sock;
    include uwsgi_params;
}


sudo nginx -t
sudo systemctl start nginx.service
sudo systemctl enable nginx.service
```


# Reference
* https://moinmo.in/HowTo/NginxWithUwsgi
* https://www.thecuriousdev.com/blog/2016/installing-moinmoin-on-nginx-and-uwsgi/