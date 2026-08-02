.DEFAULT_GOAL := help

help:
	@echo "Available commands:"
	@echo "  make build           Compile and package (skips tests)"
	@echo "  make run             Run the app locally (port 8080)"
	@echo "  make test            Run tests"
	@echo "  make lint            Check code style with Spotless"
	@echo "  make format          Auto-format code with Spotless"
	@echo "  make docker-build    Build the Docker image"
	@echo "  make docker-up       Start the app via Docker Compose"
	@echo "  make docker-down     Stop the Docker Compose stack"

build:
	./mvnw clean package -DskipTests

run:
	./mvnw spring-boot:run

test:
	./mvnw test

lint:
	./mvnw spotless:check

format:
	./mvnw spotless:apply

docker-build:
	docker compose build

docker-up:
	docker compose up

docker-down:
	docker compose down
