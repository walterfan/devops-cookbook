
curl -s get.sdkman.io|bash
sdk install springboot
spring init -a=kanban -g=com.github.walterfan -n=kanban -d=web,data-jpa,h2,thymeleaf,security -p=war kanban

apt-add-repository ppa:webupd8team/java
apt-get update
apt-get install oracle-java8-installer

mvn package

