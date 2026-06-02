# PowerShell script to run iBPMS core tests in memory-efficient segments/iterations
# @Traceability: US-005, CA-MemoryOptimization

$ErrorActionPreference = "Stop"
Clear-Host

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "🛡️ iBPMS SEGMENTED TEST RUNNER (Memory-Optimized for Laptops) 🛡️" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Este script ejecuta la suite de pruebas dividida en iteraciones independientes" -ForegroundColor Gray
Write-Host "para evitar desbordamiento de memoria (OOM) en la JVM." -ForegroundColor Gray
Write-Host ""

# Configuración de variables de entorno de JVM
$env:MAVEN_OPTS = "-Xmx512m -XX:MaxMetaspaceSize=192m"
$mvnPath = ".\maven\apache-maven-3.9.6\bin\mvn.cmd"

if (-not (Test-Path $mvnPath)) {
    Write-Host "[!] Advertencia: No se encontró Maven en $mvnPath. Usando 'mvn' global..." -ForegroundColor Yellow
    $mvnPath = "mvn"
}

Write-Host "[1] Ejecutar Pruebas de Gobernanza BPMN (Pre-flight / CA-02, CA-05, CA-09, CA-22, CA-23, CA-27, CA-33)" -ForegroundColor Green
Write-Host "[2] Ejecutar Pruebas de Workflow y Aprobaciones (CA-69 / DeployRequestWorkflowTest, DeployRequestIntegrationTest)" -ForegroundColor Green
Write-Host "[3] Ejecutar Ambos Bloques Secuencialmente (Con liberación de memoria)" -ForegroundColor Green
Write-Host "[4] Ejecutar una clase de prueba específica" -ForegroundColor Green
Write-Host "[q] Salir" -ForegroundColor Gray
Write-Host ""

$choice = Read-Host "Selecciona una opción [1-4, q]"

switch ($choice) {
    "1" {
        Write-Host "`n[+] Iniciando Iteración 1: Gobernanza BPMN..." -ForegroundColor Cyan
        Start-Process -FilePath $mvnPath -ArgumentList "test -pl ibpms-core -Dtest=DeployInvalidBpmnGovernanceCA02Test,DeployNomenclatureGovernanceCA05Test,BpmnStructuralGovernanceCA09Test,BpmnZombieNodeGovernanceCA22Test,BpmnInfiniteLoopGovernanceCA23Test,BpmnGatewayConvergenceGovernanceCA27Test,PreFlightLintingGovernanceCA33Test -DargLine=`"-Xmx512m -XX:MaxMetaspaceSize=192m`"" -NoNewWindow -Wait
    }
    "2" {
        Write-Host "`n[+] Iniciando Iteración 2: Workflow de Aprobación..." -ForegroundColor Cyan
        Start-Process -FilePath $mvnPath -ArgumentList "test -pl ibpms-core -Dtest=DeployRequestWorkflowTest,DeployRequestIntegrationTest -DargLine=`"-Xmx512m -XX:MaxMetaspaceSize=192m`"" -NoNewWindow -Wait
    }
    "3" {
        Write-Host "`n[+] Ejecutando secuencia de iteraciones..." -ForegroundColor Cyan
        
        Write-Host "`n--- Iteración 1: Gobernanza BPMN ---" -ForegroundColor Cyan
        Start-Process -FilePath $mvnPath -ArgumentList "test -pl ibpms-core -Dtest=DeployInvalidBpmnGovernanceCA02Test,DeployNomenclatureGovernanceCA05Test,BpmnStructuralGovernanceCA09Test,BpmnZombieNodeGovernanceCA22Test,BpmnInfiniteLoopGovernanceCA23Test,BpmnGatewayConvergenceGovernanceCA27Test,PreFlightLintingGovernanceCA33Test -DargLine=`"-Xmx512m -XX:MaxMetaspaceSize=192m`"" -NoNewWindow -Wait
        
        Write-Host "`n[+] Iteración 1 finalizada. Esperando 5 segundos para liberar recursos..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
        
        Write-Host "`n--- Iteración 2: Workflow de Aprobación ---" -ForegroundColor Cyan
        Start-Process -FilePath $mvnPath -ArgumentList "test -pl ibpms-core -Dtest=DeployRequestWorkflowTest,DeployRequestIntegrationTest -DargLine=`"-Xmx512m -XX:MaxMetaspaceSize=192m`"" -NoNewWindow -Wait
    }
    "4" {
        $testClass = Read-Host "Ingresa el nombre exacto de la clase de prueba a ejecutar (Ej: DeployRequestWorkflowTest)"
        if ($testClass -ne "") {
            Write-Host "`n[+] Ejecutando test individual: $testClass..." -ForegroundColor Cyan
            Start-Process -FilePath $mvnPath -ArgumentList "test -pl ibpms-core -Dtest=$testClass -DargLine=`"-Xmx512m -XX:MaxMetaspaceSize=192m`"" -NoNewWindow -Wait
        }
    }
    "q" {
        Write-Host "Ejecución cancelada." -ForegroundColor Yellow
        exit
    }
    default {
        Write-Host "Opción inválida." -ForegroundColor Red
    }
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "🏁 Proceso finalizado." -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
