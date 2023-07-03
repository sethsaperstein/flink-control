#!/bin/bash

DOCKER_IMAGE_NAME=flink-control-api
DOCKER_IMAGE_TAG=local

docker build -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG .
minikube image load $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_TAG
