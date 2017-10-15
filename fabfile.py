from fabric.api import *
from fabric.context_managers import *
from fabric.contrib.console import confirm 
import os, subprocess

local_path = os.path.dirname(os.path.abspath(__file__))
local_dir = os.getcwd()

backend_service_ports={
"tomcat": "8080",
"kanban": "8080",
"cassandra": "9042",
"elasticsearch": "9200 9300",
"influxdb": "8086",
"postgres": "5432",
"rabbitmq": "4369 5671 5672 15671 15672 25672",
"redis": "6379",
"riak": "8087 8098",
"kafka-zookeeper": "2181 9092"
}

need_print_cmd=True
only_display_cmd=False

docker_image_prefix="walterfan-"
docker_container_prefix="msa-"

restart_policy="--restart always"
jenkins_volume_mapping = "/home/walter/workspace/jenkins:/var/jenkins_home"
jenkins_container_name="jenkins"
jenkins_image_name="walterfan-jenkins"

def run_cmd(cmd):
	if(need_print_cmd):
		print cmd
	if not only_display_cmd:
		local(cmd)


@task
def jenkins_build():
	docker_build("jenkins")

@task
def jenkins_run(listen_port="1980"):
	cmd = "docker run %s -v %s -p %s:8080 -p 50000:50000 --name=%s -d %s" % (restart_policy, jenkins_volume_mapping, listen_port, jenkins_container_name, jenkins_image_name)
	run_cmd(cmd)


@task
def jenkins_start():
	cmd = "docker start %s" % jenkins_container_name
	run_cmd(cmd)

@task
def jenkins_stop():
	cmd = "docker stop %s" % jenkins_container_name
	local(cmd)
	#cmd = "docker cp jenkins-container:/var/log/jenkins/jenkins.log jenkins.log"
	#local(cmd)

@task
def jenkins_remove():
	docker_remove(jenkins_container_name)

@task
def jenkins_commit(message):
	cmd = "docker commit -m \"%s\" %s walterfan/jenkins:1.0" % (message, jenkins_container_name)

@task
def jenkins_check():
	cmd = "docker exec %s ps -ef | grep java" % jenkins_container_name
	print(cmd)
	local(cmd)

	cmd = "docker exec %s cat /var/jenkins_home/secrets/initialAdminPassword" % jenkins_container_name
	print(cmd)
	local(cmd)



#-----------------------------grafana influx --------------------------#
@task
def graflux_build():
	cmd = "docker build --tag %s docker/%s" % ("graflux", "graflux")
	run_cmd(cmd)


@task
def graflux_start():
	grafana_port = 3000
	influx_api_port = 8086
	influx_web_port = 8083
	cmd = "docker run --name local-graflux -d -p %d:3000 -p %d:8086 -p %d:8083 graflux" % (grafana_port, influx_api_port, influx_web_port)
	print(cmd)
	local(cmd)

@task
def influx():
	"""
	execute the influx command in graflux docker
	"""
	cmd = "docker exec -it local-graflux influx"
	run_cmd(cmd)

@task
def graflux_bash():
	"""
	execute the /bin/bash in graflux docker
	"""
	cmd = "docker exec -it local-graflux /bin/bash"
	run_cmd(cmd)

@task
def graflux_stop():
	#cmd = "docker stop local-graflux"
	docker_remove("local-graflux")

@task
def redis_cli():
	cmd = "docker exec -it local-redis redis-cli"
	local(cmd)	
@task
def redis_bash():
	cmd = "docker exec -it local-redis /bin/bash"
	local(cmd)	
#---------------------------- freeswitch -------------------------------#
#bettervoice/freeswitch-container   1.6.16
@task
def freeswitch_start():
	cmd = "sudo docker run --name freeswitch -p 5060:5060/tcp -p 5060:5060/udp -p 5080:5080/tcp -p 5080:5080/udp -p 8021:8021/tcp \
	-p 7443:7443/tcp -p 60535-65535:60535-65535/udp \
	-v %s/etc/freeswitch:/usr/local/freeswitch/conf bettervoice/freeswitch-container:1.6.16" % local_path
	print(cmd)
	local(cmd)

@task
def freeswitch_stop():
	docker_remove(freeswitch) 
#-----------------------------------------------------------#
@task
def start_services():
	cmd = "docker-compose up -d"
	run_cmd(cmd)

@task
def stop_services():
	cmd = "docker-compose down -v"
	run_cmd(cmd)
#----------------------------- general command ----------------

@task
def link_war(war_package, war_name):
    cmd = "docker exec tomcat ln -s %s/%s /usr/local/tomcat/webapps/%s" % (local_path, war_package, war_name)
    local(cmd)

@task
def deploy_war(war_package, war_name):
    cmd = "docker cp %s/%s tomat:/usr/local/tomcat/webapps/%s" % (local_path, war_package, war_name)
    local(cmd)

@task
def undeploy_war(war_name):
    cmd = "docker exec tomcat rm -rf /usr/local/tomcat/webapps/%s" % (war_name)
    local(cmd)
    cmd = "docker exec tomcat rm -f /usr/local/tomcat/webapps/%s.war" % (war_name)
    local(cmd)
#----------------------------- general commands ---
def get_container_id(container_name):
	str_filter = "-aqf name=%s" % container_name;
	arr_cmd = ["docker", "ps", str_filter]
	container_id = subprocess.check_output(arr_cmd).strip()
	return container_id

def get_port_args(service_name="kanban", increment=0):
	str_port = ""
	ports = backend_service_ports[service_name]
	if ports:
		arr_port = ports.split("\\s")
		for port in arr_port:
			str_port = str_port + "-p %s:%d" %(port, int(port) + int(increment))
	return str_port

@task
def kanban_build(service_name="kanban"):
	code_dir = "examples/%s" % service_name
	container_id = get_container_id(service_name)
	with lcd(code_dir):
		local("git pull origin master")
		local("mvn package")
		local("docker cp ./target/%s*.war ../../web/apps/%s.war" % (service_name, service_name))


@task
def docker_rename(old_name, new_name):
	cmd = "docker tag %s %s" % (old_name, new_name)
	run_cmd(cmd)


@task
def docker_build(service_name="tomcat"):
	docker_image_name = docker_image_prefix + service_name
	cmd = "docker build --tag %s docker/%s" % (docker_image_name, service_name)
	run_cmd(cmd)


@task
def docker_run(service_name="tomcat", volume_args="-v /workspace:/workspace"):
	port_args = get_port_args(service_name)
	
	docker_container_name = docker_container_prefix + service_name
	docker_image_name = docker_image_prefix + service_name

	cmd = "docker run %s %s %s -d --name %s %s" % (restart_policy, volume_args, port_args, docker_container_name, docker_image_name)
	run_cmd(cmd)

@task
def docker_stop(container_name="tomcat"):
	cmd = "docker stop %s" % (container_name)
	run_cmd(cmd)

@task
def docker_list(container_name="tomcat"):
	cmd = "docker ps %s" % (container_name)
	run_cmd(cmd)

@task
def docker_exec(container_name="tomcat", instruction="/bin/bash"):

	instruction = "/bin/bash"
	cmd = "docker exec -it %s %s" % (container_name, 	instruction)
	run_cmd(cmd)

@task
def docker_remove(container_name="kanban"):
	cmd1 = "docker kill %s|| true" % container_name
	run_cmd(cmd1)

	cmd2 = "docker rm -v %s || true" % container_name
	run_cmd(cmd2)

@task
def docker_commit(container_id, image_name, message=""):
	cmd = "docker commit -m \"%s\" %s %s" % (message, container_id, image_name)
	run_cmd(cmd)

@task
def docker_install():
	#cmd  ="brew remove docker && brew upgrade"
	cmd = "brew cask install docker && open /Applications/Docker.app"
	run_cmd(cmd)    

@task
def help():
	print "examples:"
	print "\tfab docker_run:cassandra,\"-v /home/walter:/workspace\" "
