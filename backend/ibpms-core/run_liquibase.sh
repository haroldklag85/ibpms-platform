#!/bin/bash
mvn process-resources && mvn liquibase:update \
  -Dliquibase.url=jdbc:postgresql://localhost:5433/ibpms_e2e \
  -Dliquibase.username=ibpms \
  -Dliquibase.password=ibpms_e2e_pass \
  -Dliquibase.changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml
