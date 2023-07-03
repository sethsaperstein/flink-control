integ:
	./src/test/integ/setup.sh
	./src/test/integ/build.sh
	./src/test/integ/test.sh

test-local:
	./src/test/integ/build.sh
	./src/test/integ/test.sh

test:
	./src/test/integ/test.sh

teardown:
	kubectl delete -f ./src/test/integ/k8s/pod.yaml
	pkill -f "kubectl port-forward"
