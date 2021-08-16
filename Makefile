.SILENT:
.PHONY: usage
usage:
	@echo "Usage:"
	@echo "    make mac"
	@echo "    make amzn"

.PHONY: mac
amzn:
	mkdir -p jars
	cd jdf/ && cargo build --release && cd ../
	cp target/release/libjdf_sys.dylib src/main/resources/libjdf_sys-apple.dylib
	sbt assembly

.PHONY: amzn
amzn:
	mkdir -p jars
	docker build . -t s-jdf:amzn_build -f docker/Dockerfile.amazon
	docker run s-jdf:amzn_build > jars/s-jdf-assembly-amazonlinux.jar
	docker rmi -f s-jdf:amzn_build

.PHONY: libmac
libmac:
	mkdir -p jars
	cd jdf/ && cargo build --release -p jdf-ffi && cd ../
	cp jdf/target/release/libjdf_sys.dylib src/main/resources/libjdf_sys-apple.dylib

.PHONY: libamzn
libamzn:
	mkdir -p jars
	docker build . -t s-jdf:amzn_build -f docker/Dockerfile.amazon.comple
	docker run s-jdf:amzn_build > src/main/resources/libjdf_sys-linux.so
	docker rmi -f s-jdf:amzn_build
