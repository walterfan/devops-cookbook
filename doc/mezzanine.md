

# How to use Mezzanine
## install python and virtualenv
* take ubuntu as example

```
apt install python3
pip3 install virtualenv
#then install the required libraries

virtualenv -p python3 venv
source venv/bin/activate
pip install mezzanine

```

## create your project

```
mezzanine-project pims
```

## Make configuration

```
cd pims
vi pims/local_settings.py
```

* You need to add your server address into ALLOWED_HOSTS=['1.2.3.4']

```
ALLOWED_HOSTS=['1.2.3.4']
SITE_PREFIX = 'min'
```

* vi pims/urls.py

```
url("^min/admin/", include(admin.site.urls)),

urlpatterns += [


    url("^min/$", direct_to_template, {"template": "index.html"}, name="home"),
    url("^%s/" % settings.SITE_PREFIX, include("mezzanine.urls"))
```

* then create db and start the server


```
python manage.py createdb --noinput
python manage.py runserver 1.2.3.4:8080
```

then write a startup.sh

```
gunicorn --bind 0.0.0.0:8080 pims.wsgi &

```

# Install nginx and make configration


* install nginx

```
apt install nginx

```

* vi /etc/nginx/sites-available/default

```
    server {
        listen 80 default_server;
        listen [::]:80 default_server;
        
           root /var/www/html;

        # Add index.php to the list if you are using PHP
        index index.html index.htm index.nginx-debian.html;

        server_name _;

        location /min {
                # First attempt to serve request as file, then
                # as directory, then fall back to displaying a 404.
                #try_files $uri $uri/ =404;
                proxy_pass http://10.224.112.66:8080;
                proxy_redirect off;
        }

        location /static {
                alias /home/walter/pims/static/;
        }    
   }
                                        
```

then reload the configuration

```

nginx -s reload
```