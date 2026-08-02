.DEFAULT_GOAL := help

APP_DIR := polizas-api

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
	cd $(APP_DIR) && ./mvnw clean package -DskipTests

run:
	cd $(APP_DIR) && ./mvnw spring-boot:run

test:
	cd $(APP_DIR) && ./mvnw test

lint:
	cd $(APP_DIR) && ./mvnw spotless:check

format:
	cd $(APP_DIR) && ./mvnw spotless:apply

docker-build:
	docker compose build

docker-up:
	docker compose up

docker-down:
	docker compose down
