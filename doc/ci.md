# Background
## What's CI
Continuous integration automates the builds, provides feedback via code review, and automates code quality and security tests. It creates a release package that is ready to be deployed to your production environment.

## What's CD
Continuous delivery automatically provisions infrastructure, manages infrastructure changes, ticketing, and release versioning. It allows, progressive code deployment, verifying and monitoring changes made and providing the ability to roll back when necessary. Together, GitLab Continuous Integration and Delivery help you automate your SDLC, making it repeatable and on-demand with minimal manual intervention


# CI 环境
  1. build server(gitlab runner)
  2. TA environment 用来做 merge request 的验证

# CI 工具
  - merge request 可触发 build pipeline（包括代码静态检查，单元测试， api test, document generation, etc.）
  - 只有 build pipeline 通过， merge request approve, 代码才可合并到主分支(master branch)
  1. 使用 Jenkins 和 Gitlab runner 来驱动整个 build pipeline
  2. 使用 ansible 来进行系统依赖的安装配置，并部署软件包

# CI 步骤
  1. CI 自动化流水线的构建: 
    1. Jenkins and Gitlab runner 
  2. 代码规范检查和静态代码扫描
  3. 单元测试
  4. API 测试
    1. Python script to do API testing via MQTT, HTTP or WebSocket API
  5. 自动生成文档
  6. 自动上传制成品

# CD 步骤
  1. 自动配置环境
  2. 自动安装软件包
  3. 自动修改配置
  4. 自动运行并验证

# Practice
## C++ 项目
- CI 流程由 Jenkinsfile 定义
- 代码规范检查和静态检查，可使用 cppcheck, cpplint
- 单元测试 - google test
- API 测试 - python unittest
- Doxygen 文档生成
- 自动打包上传生成的软件包
- 自动部署至本地测试环境

## Java 项目
- 代码规范检查和静态检查
  - 可使用 maven 的 checkstyle, findbug, pmd 插件
- 单元测试
  - 通过 junit 或 testng 组织测试用例
- API 测试
  - 通过 python 测试 http 和 mqtt API
- 自动生成文档
  - 通过 javadoc, swagger 
- 自动打包上传生成的软件包 mvn upload
- 自动部署至本地测试环境 mvn deploy


