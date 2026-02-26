# Makefile for iBPMS Platform

# Variables
DOCKER_COMPOSE = docker compose

.PHONY: all build up down logs test lint clean

all: help

help:
	@echo "Available targets:"
	@echo "  build   - Build Docker images"
	@echo "  up      - Start services with docker-compose"
	@echo "  down    - Stop and remove containers"
	@echo "  logs    - Show logs of services"
	@echo "  test    - Run integration tests (Testcontainers)"
	@echo "  lint    - Lint Dockerfiles and shell scripts"
	@echo "  clean   - Remove Docker images and volumes"

build:
	$(DOCKER_COMPOSE) build

up:
	$(DOCKER_COMPOSE) up -d

down:
	$(DOCKER_COMPOSE) down -v

logs:
	$(DOCKER_COMPOSE) logs -f

# Placeholder for Testcontainers integration tests (Java Maven)
# Assumes Maven wrapper is present
# Adjust the path to your backend module if needed

test:
	cd backend/ibpms-core && ./mvnw verify

lint:
	# Lint Dockerfile using hadolint
	hadolint -c .hadolint.yaml Dockerfile || true
	# Lint shell scripts (if any)
	find . -name "*.sh" -exec shellcheck {} + || true

clean:
	$(DOCKER_COMPOSE) rm -sf
	docker image prune -f
	docker volume prune -f
