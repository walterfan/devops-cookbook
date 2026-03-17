# DevOps Cookbook 🛠️

```{mermaid}
mindmap
  root((DevOps Cookbook))
    Container
      Docker
      Podman
      Registry
    Orchestration
      Kubernetes
      kubectl
      Helm
      Operator
    Infrastructure
      Minikube
      etcd
      Ingress
      Service Mesh
    Tech Stacks
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

A practical, recipe-driven guide to modern DevOps tools and tech stacks. Each chapter provides hands-on commands, configuration snippets, and real-world patterns you can copy-paste into your projects.

> 🇨🇳 [中文版](../zh/index)

## How to Use This Book

- **Part 1**: Container fundamentals — Docker, Podman, image building, registry
- **Part 2**: Kubernetes core — architecture, kubectl, workloads, networking, storage
- **Part 3**: K8s ecosystem — Helm, Operators, Ingress, etcd, monitoring
- **Part 4**: Popular tech stacks — LNMP, ELK, Go/Python/Java on K8s
- **Part 5**: CI/CD & GitOps — pipelines, ArgoCD, best practices

```{toctree}
:maxdepth: 2
:caption: "Part 1: Containers"

chapters/ch01_docker_basics
chapters/ch02_dockerfile_best_practices
chapters/ch03_docker_compose
chapters/ch04_podman
chapters/ch05_container_registry
```

```{toctree}
:maxdepth: 2
:caption: "Part 2: Kubernetes Core"

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
:caption: "Part 3: K8s Ecosystem"

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
:caption: "Part 4: Tech Stacks"

chapters/ch20_lnmp_stack
chapters/ch21_lnpp_pgvector
chapters/ch22_elk_stack
chapters/ch23_golang_on_k8s
chapters/ch24_python_on_k8s
chapters/ch25_java_on_k8s
```

```{toctree}
:maxdepth: 2
:caption: "Part 5: CI/CD & GitOps"

chapters/ch26_cicd_pipelines
chapters/ch27_argocd_gitops
chapters/ch28_security_best_practices
chapters/ch29_troubleshooting
chapters/ch30_production_checklist
```
