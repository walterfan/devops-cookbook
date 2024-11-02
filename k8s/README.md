# k8s quick start



## Example 1
```
kubectl get pods
kubectl apply -f nginx_pod.yaml

```

* nginx_pod.yaml


* 查看 pod 列表

```
kubectl get pod -l run=entry-nginx -o wide
```

## example 2
note: we ignore `-n <namespace>` option, so the following commands run in default namespace.

* create and push docker image
```shell
cd hellonode
docker image build -t hfrtc/hellonode:1.0 .
docker login
docker image push hfrtc/hellonode:1.0
```

* create pod

```shell
kubectl apply -f hellonode_pod.yaml
kubectl get pods

> NAME         READY   STATUS    RESTARTS   AGE
> hello-node   1/1     Running   0          5m5s

kubectl describe pod hello-node
```
* create service

```shell
# create local service for hello-node pod
kubectl apply -f hellonode_svc.yaml
kubectl get svc
```
* cleanup

```shell
kubectl delete pod hello-node
kubectl delete svc hello-svc
```

* create deployment

```shell

$ kubectl apply -f hellonode-deploy.yaml
$ kubectl get deployments
----------------------------
NAME                READY   UP-TO-DATE   AVAILABLE   AGE
hello-node-deploy   3/3     3            3           6m30s

$ kubectl get pods
----------------------------
NAME                                 READY   STATUS    RESTARTS   AGE
hello-node-deploy-86cc5b6bcc-5kdc6   1/1     Running   0          70s
hello-node-deploy-86cc5b6bcc-99hpf   1/1     Running   0          70s
hello-node-deploy-86cc5b6bcc-sntlc   1/1     Running   0          70s

$ kubectl delete pod hello-node-deploy-86cc5b6bcc-5kdc6
pod "hello-node-deploy-86cc5b6bcc-5kdc6" deleted

$ kubectl get pods
----------------------------
NAME                                 READY   STATUS    RESTARTS   AGE
hello-node-deploy-86cc5b6bcc-99hpf   1/1     Running   0          2m17s
hello-node-deploy-86cc5b6bcc-mx8f7   1/1     Running   0          54s
hello-node-deploy-86cc5b6bcc-sntlc   1/1     Running   0          2m17s
```

* 清理

```shell
kubectl delete deployment hello-node-deploy
```