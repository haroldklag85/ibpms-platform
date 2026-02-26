# iBPMS Platform - QA & Security Audit Report

**Date:** 2026-02-25
**Role:** DevOps, Security & QA Agent
**Phase:** Sprint 1 Handoff

## 1. Executive Summary
This document outlines the security controls, testing infrastructure, and architectural validations implemented during the initial setup of the iBPMS Platform. The objective is to ensure a secure, reproducible, and resilient environment for both local development and forthcoming production deployments.

## 2. Docker & Container Security
The container orchestration layer (`docker-compose.yml` and `Dockerfile`) was hardened following industry best practices:

*   **Principle of Least Privilege (PoLP):** The `ibpms-db` service (MySQL 8) drops all unnecessary capabilities (`cap_drop: ALL`) and enforces `no-new-privileges:true`. Read-only filesystems are enforced where possible. 
*   **Resource Throttling:** Memory and CPU limits have been established for the database (`memory: 512M`, `cpus: 0.5`) to prevent DoS-like scenarios due to resource exhaustion gracefully.
*   **Multi-Stage Builds:** The backend `Dockerfile` utilizes a minimal JRE base image (`eclipse-temurin:17-jre-alpine`) for the runtime stage, executing entirely under a non-root `javauser`. The OS footprint has been minimized to reduce the attack surface.
*   **Persistence & Recovery:** Critical Camunda and DB data points are mapped to persistent Docker volumes (`ibpms-db-data`).

## 3. Network & Gateway Security (NGINX)
A lightweight Ingress controller was set up for HTTP routing:

*   **TLS Termination:** Configured local development certificates (self-signed) running over HTTPS (Port 443).
*   **Protocol Hardening:** Weak SSL/TLS protocols are disabled, forcing the use of TLSv1.2 and TLSv1.3 with high-strength ciphers.
*   **CORS Policies:** Configured dynamic CORS mapping restrictive to MFE development origins (`localhost:5173`, `5174`, `...`), enabling seamless yet secure local cross-origin operations.

## 4. Test Infrastructure & Quality Assurance
The testing layer has been structured to prevent regressions and simulate heavy loads:

*   **Testcontainers (Java):** Integration tests were encapsulated inside ephemeral containers (`AbstractIntegrationTest.java`). These spin up isolated instances of MySQL via JUnit 5, guaranteeing testing parity with production.
*   **API Automation:** An extensive Postman/Bruno collection was created mapped to the `openapi.yaml` contracts.
*   **External Mocking (Wiremock):** Simulators for external dependencies (CRM APIs, AI Translation for DMNs) were put in place, allowing UI development to proceed unaffected by 3rd party downtimes or rate limits.
*   **Stress Testing:** Established `k6` and Bash-based load testing scripts (targeting 200+ concurrent requests) to validate Camunda's Thread Pool limits and connection pool stability.

## 5. Dependency Audit Strategy
*   It is advised that the CI/CD pipeline actively integrates scanning utilities such as OWASP Dependency-Check or Snyk to evaluate `pom.xml` and Node.js artifacts for known CVEs. Currently, dependencies are updated to secure versions as per the BOM.

## 6. Conclusion
The environment is stabilized and hardened. The initial DevOps pipeline provides a robust baseline for the development team to safely scale the creation of Expedientes and Micro-Frontends.
