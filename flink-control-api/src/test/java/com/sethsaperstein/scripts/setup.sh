FLINK_OPERATOR_VERSION=1.5.0
KUBERNETES_VERSION=v1.25.3
MINIKUBE_VERSION=v1.30.1

# minikube setup
curl -LO https://storage.googleapis.com/minikube/releases/${MINIKUBE_VERSION}/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
minikube start --kubernetes-version=${KUBERNETES_VERSION}

# operator setup
kubectl create -f https://github.com/jetstack/cert-manager/releases/download/v1.8.2/cert-manager.yaml
helm repo add flink-operator-repo https://downloads.apache.org/flink/flink-kubernetes-operator-${FLINK_OPERATOR_VERSION}/
helm install flink-kubernetes-operator flink-operator-repo/flink-kubernetes-operator
