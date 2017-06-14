FROM jenkins:2.46.3

MAINTAINER Walter Fan

USER root

RUN apt-get update
RUN apt-get -y install python-pip
RUN apt-get -y install fabric

RUN mkdir /var/log/jenkins
RUN mkdir /var/cache/jenkins
RUN chown -R jenkins:jenkins /var/log/jenkins
RUN chown -R jenkins:jenkins /var/cache/jenkins
USER jenkins

ENV JAVA_OPTS="-Xmx1596m"
