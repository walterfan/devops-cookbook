from fabric.api import *
from fabric.context_managers import *
from fabric.contrib.console import confirm 
import os, subprocess


file_list = []
git_changed_files = subprocess.check_output("git status -s -uno", shell=True)

for git_changed_file in git_changed_files.split('\n'):
    filenames = git_changed_file.split(' ')
    if len(filenames) > 2:
        filename = filenames[2]
        file_list.append(filename.strip())

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

restart_policy="--restart always"
jenkins_volume_mapping = "/Users/walterfan/Documents/workspace:/workspace/jenkins:/var/jenkins_home"
jenkins_container_name=""
jenkins_image_name=""

@task
def jenkins_build():
	cmd = "docker build --tag jenkins-image ./docker/jenkins"
	print cmd
	local(cmd)

@task
def jenkins_start():
	cmd = "docker start jenkins-container"
	print cmd
	local(cmd)

def jenkins_run(listen_port="1980"):
	cmd = "docker run %s -v %s -p %s:8080 -p 50000:50000 --name=jenkins-container -d jenkins-image" % (restart_policy, jenkins_volume_mapping, listen_port)
	local(cmd)

@task
def jenkins_stop():
	cmd = "docker stop jenkins-container"
	local(cmd)
	#cmd = "docker cp jenkins-container:/var/log/jenkins/jenkins.log jenkins.log"
	#local(cmd)

@task
def jenkins_remove():
	rm_docker("jenkins-container")

@task
def jenkins_commit(message):
	cmd = "docker commit -m \"%s\" 8b74772fd434 walterfan/jenkins:1.0" % (message, )

@task
def jenkins_check():
	cmd = "docker exec jenkins-container ps -ef | grep java"
	print cmd
	local(cmd)

	cmd = "docker exec jenkins-container cat /var/jenkins_home/secrets/initialAdminPassword"
	print cmd
	local(cmd)

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
def build(service_name="kanban"):
	code_dir = "examples/%s" % service_name
	container_id = get_container_id(service_name)
	with lcd(code_dir):
		local("git pull origin master")
		#local("mvn clean package")
		local("docker cp ./target/%s*.war %s:/usr/local/tomcat/webapps/%s.war" % (service_name, container_id, service_name))


@task
def build_docker(service_name="kanban"):
	cmd = "docker build --tag %s docker/%s" % (service_name, service_name)
	print cmd
	local(cmd)



@task
def run_docker(service_name="kanban"):
	str_port_args = get_port_args(service_name)
	cmd = "docker run %s -v %s -d --name %s %s %s" % (restart_policy, volume_mapping, service_name, str_port_args, service_name)
	print cmd
	local(cmd)

@task
def stop_docker(service_name="kanban"):
	cmd = "docker stop %s" % (service_name)
	print cmd
	local(cmd)

@task
def list_docker(service_name="kanban"):
	cmd = "docker stop %s" % (service_name)
	print cmd
	local(cmd)

@task
def exec_docker(container_name="kanban", instruction="/bin/bash"):

	contain_id = get_container_id(container_name)

	cmd = "docker exec %s -t -i %s" % (contain_id, instruction)
	print cmd
	local(cmd)

@task
def rm_docker(container_name="kanban"):
	cmd1 = "docker kill %s|| true" % container_name
	print cmd1
	local(cmd1)
	cmd2 = "docker rm -v %s || true" % container_name
	print cmd2
	local(cmd2)

@task
def commit_docker(container_id, message):
	cmd = "docker commit -m \"%s\" %s walterfan/jenkins:1.0" % (message, container_id)
	print cmd
	local(cmd)

@task
def install_docker():
	#cmd  ="brew remove docker && brew upgrade"
	cmd = "brew cask install docker && open /Applications/Docker.app"
	print cmd
	local(cmd)    