# quick start

```
kubectl apply -f test_pod.yaml

```

* test_pod.yaml

```
  apiVersion: apps/v1
  kind: Deployment
  metadata:
   name: entry-nginx
  spec:
   selector:
     matchLabels:
       run: entry-nginx
   replicas: 2
   template:
     metadata:
       labels:
         run: entry-nginx
     spec:
       containers:
         - name: entry-nginx
           image: nginx
           imagePullPolicy: IfNotPresent
           ports:
             - containerPort: 80
```

* 查看 pod 列表

```
kubectl get pod -l run=entry-nginx -o wide
```