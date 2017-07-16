# Quick start
------------------------------------------
* mvn archetype:generate -DgroupId=com.github.walterfan.kanban -DartifactId=server -DarchetypeArtifactId=maven-archetype-templates -DinteractiveMode=false

* mvn archetype:generate -DgroupId=com.github.walterfan.kanban -DartifactId=client -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

* mvn archetype:generate -DgroupId=com.github.walterfan.kanban -DartifactId=common -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

mvn archetype:generate -DgroupId=com.github.walterfan.kanban -DartifactId=test -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false

* mvn eclipse:eclipse

* mvn -U install -Dmaven.test.skip=true;
* cd server
* mvn jetty:run

# Dependencies
--------------------------------------------

Spring
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* Spring MVC

AngularJS
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
* refer to Pro AngularJS

1. overview
* angular module: run, filter
* application: ng-app
* model: ng-model, ng-repeat, filter, orderBy
* controller: ng-controller, ng-click
* view: ng-hide, ng-class

* Bootstrap
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



