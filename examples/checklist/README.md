


# Spring boot

```
curl -s "https://get.sdkman.io" | bash
# open a new terminal window
sdk install springboot
```

spring --version
Spring CLI v1.4.4.RELEASE

or 

```
$ brew tap pivotal/tap
$ brew install springboot
```

Create a project by spring cli

```
spring init --build=maven --java-version=1.8 --dependencies=web,security --packing=jar --groupId=com.github.walterfan --artifactId=checklist
```

