import os
import sys
from fabric import task
from fabric import Connection
import time
import json
import logging
import socket
import os, subprocess

default_hosts = ["localhost"]
local_path = os.path.dirname(os.path.abspath(__file__))
local_dir = os.getcwd()

need_print_cmd=True
only_display_cmd=False

def run_cmd(c, cmd):
	if(need_print_cmd):
		print(cmd)
	if not only_display_cmd:
		c.local(cmd)

def get_local_ip():
	s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	s.connect(("8.8.8.8", 80))
	local_ip = s.getsockname()[0]
	s.close()
	return local_ip

@task(hosts=default_hosts)
def plantuml(c, port=1975):
	cmd = f"docker run -d -p {port}:8080 plantuml/plantuml-server:tomcat"
	run_cmd(c, cmd)

@task(hosts=default_hosts)
def srs_webrtc(c):
	candidate = get_local_ip()
	#cmd = 'CANDIDATE="{}"'.format(candidate)
	cmd = f'docker run --rm -i -p 1935:1935 -p 1985:1985 -p 8080:8080 -p 1990:1990 -p 8088:8088 \
    --env CANDIDATE={candidate} -p 8000:8000/udp \
    registry.cn-hangzhou.aliyuncs.com/ossrs/srs:5 ./objs/srs -c conf/https.docker.conf'
	run_cmd(c, cmd)

