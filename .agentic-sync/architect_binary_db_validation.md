# Agentic Handoff Request: Evaluación de Arquitectura de Almacenamiento de Binarios

**To:** Lead Software Architect Agent
**From:** Product Owner (PO)
**Related US:** US-039 (Formulario Genérico Base y Adjuntos)
**Date:** 2026-03-07

## Contexto de Negocio
Actualmente, las tareas del iBPMS permiten adjuntar evidencia documental en formularios genéricos (Pantalla 7.B). En los refinamientos de requerimientos (US-039), se determinó funcionalmente que estos adjuntos "quedan como binarios en una estructura de archivos asociada a cada proyecto".

## Solicitud de Arquitectura
El Product Owner solicita explícitamente una validación técnica y recomendación de mejores prácticas sobre el siguiente enfoque:

1. **¿Debería existir dentro del clúster de bases de datos una Base de Datos exclusiva / dedicada (Ej: MongoDB GridFS, Postgres con persistencia pura de Blobs, etc) SOLO para almacenar estos binarios?**
2. **¿O por el contrario, es una mala práctica persistir binarios en motor de BD y debemos usar obligatoriamente almacenamiento de objetos puro (S3, Azure Blob Storage) o el FileSystem del SGDEA (SharePoint)?**

## Action Item para el Arquitecto
Por favor, analiza el trade-off (Costo vs Rendimiento vs Backups) de ambas estrategias para nuestro entorno `ibpms-platform`. Genera un reporte de decisión de arquitectura (ADR) con tu veredicto y actualiza los diagramas C4 correspondientes para que los agentes Backend sepan exactamente dónde persistir los `MultipartFiles`.
