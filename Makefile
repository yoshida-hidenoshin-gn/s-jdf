.SILENT:
.PHONY: usage
usage:
	@echo "Usage:"
	@echo "    make mac"
	@echo "    make amzn"

.PHONY: mac
amzn:
	cd jdf/ && cargo build --release && cd ../
	cp target/release/libjdf_sys.dylib src/main/resources/libjdf_sys-apple.dylib
	sbt assembly

.PHONY: amzn
amzn:
	docker build . -t s-jdf:amzn_build -f docker/Dockerfile.amazon
	docker run s-jdf:amzn_build > jars/s-jdf-assembly-amazonlinux.jar
	docker rmi -f s-jdf:amzn_build
