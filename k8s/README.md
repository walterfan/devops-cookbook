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