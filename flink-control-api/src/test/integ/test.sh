#!/bin/bash
set -eux

# Create rest api resources
kubectl create -f ./src/test/integ/k8s/pod.yaml -n integ
kubectl wait --for=condition=Ready pod/flink-control-api -n integ

# Expose the flink-control-api service
kubectl port-forward flink-control-api 8888:8080 -n integ &

mvn verify -Dskip.unit.tests=true

#kubectl delete -f ./src/test/integ/k8s/pod.yaml
#pkill -f "kubectl port-forward"
