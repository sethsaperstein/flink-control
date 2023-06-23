#!/bin/bash
set -eux

# Create rest api resources
kubectl create -f ./src/test/integ/k8s/pod.yaml -n integ
kubectl wait --for=condition=Ready pod/flink-control-api -n integ

# Expose the flink-control-api service
SERVICE_URL=$(minikube service flink-control-api -n integ --url | awk 'NR==1{print $NF}')
echo "service url: $SERVICE_URL"
PORT=$(echo "$SERVICE_URL" | grep -oE '[0-9]{4,5}' | awk 'NR==1')
export APP_PORT=$PORT
echo "The service is accessible on port: $PORT"

mvn verify -Dskip.unit.tests=true

kubectl delete -f ./src/test/integ/k8s/pod.yaml
