# DevOps 实战手册 🛠️

```{mermaid}
mindmap
  root((DevOps 实战手册))
    容器技术
      Docker
      Podman
      镜像仓库
    容器编排
      Kubernetes
      kubectl
      Helm
      Operator
    基础设施
      Minikube
      etcd
      Ingress
      Service Mesh
    技术栈
      LNMP/LNPP
      ELK Stack
      Go + K8s
      Python + K8s
      Java + K8s
    CI/CD
      GitLab CI
      GitHub Actions
      Jenkins
      ArgoCD
```

一本面向实战的 DevOps 工具与技术栈指南。每章提供可直接复用的命令、配置片段和真实场景模式。

> 🇬🇧 [English Version](../en/index)

## 本书结构

- **第一部分**：容器基础 — Docker、Podman、镜像构建、镜像仓库
- **第二部分**：Kubernetes 核心 — 架构、kubectl、工作负载、网络、存储
- **第三部分**：K8s 生态 — Helm、Operator、Ingress、etcd、监控
- **第四部分**：常用技术栈 — LNMP、ELK、Go/Python/Java 上 K8s
- **第五部分**：CI/CD 与 GitOps — 流水线、ArgoCD、最佳实践

```{toctree}
:maxdepth: 2
:caption: "第一部分：容器技术"

chapters/ch01_docker_basics
chapters/ch02_dockerfile_best_practices
chapters/ch03_docker_compose
chapters/ch04_podman
chapters/ch05_container_registry
```

```{toctree}
:maxdepth: 2
:caption: "第二部分：Kubernetes 核心"

chapters/ch06_k8s_architecture
chapters/ch07_minikube
chapters/ch08_kubectl
chapters/ch09_pods_and_workloads
chapters/ch10_services_and_networking
chapters/ch11_storage
chapters/ch12_configmap_and_secrets
```

```{toctree}
:maxdepth: 2
:caption: "第三部分：K8s 生态"

chapters/ch13_helm
chapters/ch14_ingress
chapters/ch15_operators
chapters/ch16_etcd
chapters/ch17_monitoring
chapters/ch18_logging
chapters/ch19_service_mesh
```

```{toctree}
:maxdepth: 2
:caption: "第四部分：技术栈"

chapters/ch20_lnmp_stack
chapters/ch21_lnpp_pgvector
chapters/ch22_elk_stack
chapters/ch23_golang_on_k8s
chapters/ch24_python_on_k8s
chapters/ch25_java_on_k8s
```

```{toctree}
:maxdepth: 2
:caption: "第五部分：CI/CD 与 GitOps"

chapters/ch26_cicd_pipelines
chapters/ch27_argocd_gitops
chapters/ch28_security_best_practices
chapters/ch29_troubleshooting
chapters/ch30_production_checklist
```
