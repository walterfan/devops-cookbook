# DevOps Cookbook 🛠️

A practical, recipe-driven guide to modern DevOps tools and tech stacks. Built with Sphinx + MyST Markdown + Mermaid diagrams.

## 📖 Contents

### Part 1: Containers
- [Ch01: Docker 基础](doc/source/chapters/ch01_docker_basics.md)
- [Ch02: Dockerfile 最佳实践](doc/source/chapters/ch02_dockerfile_best_practices.md)
- [Ch03: Docker Compose 编排](doc/source/chapters/ch03_docker_compose.md)
- [Ch04: Podman 容器引擎](doc/source/chapters/ch04_podman.md)
- [Ch05: 容器镜像仓库](doc/source/chapters/ch05_container_registry.md)

### Part 2: Kubernetes Core
- [Ch06: Kubernetes 架构](doc/source/chapters/ch06_k8s_architecture.md)
- [Ch07: Minikube 本地开发](doc/source/chapters/ch07_minikube.md)
- [Ch08: kubectl 命令大全](doc/source/chapters/ch08_kubectl.md)
- [Ch09: Pod 与工作负载](doc/source/chapters/ch09_pods_and_workloads.md)
- [Ch10: Service 与网络](doc/source/chapters/ch10_services_and_networking.md)
- [Ch11: 存储管理](doc/source/chapters/ch11_storage.md)
- [Ch12: ConfigMap 与 Secret](doc/source/chapters/ch12_configmap_and_secrets.md)

### Part 3: K8s Ecosystem
- [Ch13: Helm 包管理](doc/source/chapters/ch13_helm.md)
- [Ch14: Ingress 入口控制](doc/source/chapters/ch14_ingress.md)
- [Ch15: Kubernetes Operator](doc/source/chapters/ch15_operators.md)
- [Ch16: etcd 分布式存储](doc/source/chapters/ch16_etcd.md)
- [Ch17: 监控体系](doc/source/chapters/ch17_monitoring.md)
- [Ch18: 日志收集](doc/source/chapters/ch18_logging.md)
- [Ch19: Service Mesh 服务网格](doc/source/chapters/ch19_service_mesh.md)

### Part 4: Tech Stacks
- [Ch20: LNMP 技术栈](doc/source/chapters/ch20_lnmp_stack.md) (Linux + Nginx + MySQL + PHP/Python)
- [Ch21: LNPP + pgvector 向量搜索栈](doc/source/chapters/ch21_lnpp_pgvector.md)
- [Ch22: ELK/ELKK 技术栈](doc/source/chapters/ch22_elk_stack.md)
- [Ch23: Go 应用上 K8s](doc/source/chapters/ch23_golang_on_k8s.md)
- [Ch24: Python 应用上 K8s](doc/source/chapters/ch24_python_on_k8s.md)
- [Ch25: Java 应用上 K8s](doc/source/chapters/ch25_java_on_k8s.md)

### Part 5: CI/CD & GitOps
- [Ch26: CI/CD 流水线](doc/source/chapters/ch26_cicd_pipelines.md)
- [Ch27: ArgoCD 与 GitOps](doc/source/chapters/ch27_argocd_gitops.md)
- [Ch28: 安全最佳实践](doc/source/chapters/ch28_security_best_practices.md)
- [Ch29: 故障排查手册](doc/source/chapters/ch29_troubleshooting.md)
- [Ch30: 生产环境检查清单](doc/source/chapters/ch30_production_checklist.md)

## 🚀 Quick Start

```bash
# Install dependencies
pip install -r doc/requirements.txt

# Build HTML
cd doc && make html

# Open in browser
open build/html/index.html

# Live reload (for writing)
make livehtml
```

## 🏗️ Tech Stack

- **Sphinx** — Documentation generator
- **MyST Markdown** — Markdown parser for Sphinx
- **Mermaid** — Diagrams as code
- **sphinx-book-theme** — Clean, readable theme
- **sphinx-copybutton** — Copy button for code blocks

## 📁 Project Structure

```
devops-cookbook/
├── doc/
│   ├── source/
│   │   ├── conf.py              # Sphinx config
│   │   ├── index.md             # Table of contents
│   │   └── chapters/            # 30 chapters (MyST Markdown)
│   ├── build/html/              # Generated HTML
│   ├── Makefile
│   └── requirements.txt
├── docker/                      # Docker Compose examples
├── k8s/                         # K8s manifest examples
├── examples/                    # Application examples
├── ansible/                     # Ansible playbooks
└── script/                      # Utility scripts
```

## License

MIT
