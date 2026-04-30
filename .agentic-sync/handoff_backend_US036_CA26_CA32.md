# 🏗️ Handoff Técnico Backend: US-036 (CA-26 al CA-32)

## 1. Metadatos y SSOT
- **Iteración:** 90-DEV-sprint6
- **Rama Git:** `sprint-6/uat-certification`
- **US:** US-036 (Identity Governance)
- **CAs:** CA-26, CA-27, CA-28, CA-29, CA-30, CA-31, CA-32
- **Exclusiones:** Funcionalidades V2, sub-menús de granularidad fina, RBAC basado en atributos (ABAC).
- **SSOT:** `docs/requirements/epics/epic_E_seguridad_identidad_config.md`
- **Flujo de Trabajo:** 1️⃣ Backend (Tú inicias primero).

## 2. Alineación Arquitectónica y ADRs
- **ADR-001 (Hexagonal Architecture):** La lógica de cálculo de menú y unión de permisos DEBE vivir en `domain/` o `application/service/`, nunca en el `Controller`.
- **CA-31 (Anti-JWT Bloat) & Zero-Trust:** Prohibido agregar la matriz de menús a los claims del token JWT. La topología debe consumirse vía un endpoint dinámico para permitir la revocación inmediata.
- **CA-30 (Unión Matemática Multirrol):** Si un usuario tiene múltiples roles (ej. `PROCESS_OWNER` + `AUDITOR`), el backend consolida y devuelve el "superconjunto" de menús macro sin duplicados.
- **CA-27 (Inmutabilidad):** Reglas duras para evitar alteraciones a los menús atados a perfiles como `SUPER_ADMIN`.

## 3. Rutas Exactas y Contexto Preexistente
- `ibpms-platform/backend/src/main/java/.../IdentityGovernanceController.java` (o equivalente): Deberá exponer `GET /api/v1/users/me/menu-layout`.
- `ibpms-platform/backend/src/main/java/.../application/service/MenuLayoutService.java`: Deberá contener la lógica de negocio para la extracción y set-merging de permisos macro.

## 4. Snippets Prescriptivos (El "Qué" y el "Cómo")

**Endpoint Dinámico:**
```java
@RestController
@RequestMapping("/api/v1/users/me")
public class MenuTopologyController {
    // Inject MenuLayoutService
    @GetMapping("/menu-layout")
    public ResponseEntity<MenuTopologyDTO> getMyMenuTopology(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(menuLayoutService.computeTopologyForUser(user.getUsername()));
    }
}
```

**Caché Redis L2 (CA-32):**
```java
@Service
public class MenuLayoutService {
    @Cacheable(value = "menuTopology", key = "#username")
    public MenuTopologyDTO computeTopologyForUser(String username) {
        // 1. Obtener todos los roles asignados al usuario
        // 2. Ejecutar UNION SQL o Set merging en Java (CA-30)
        // 3. Garantizar Inmutabilidad de Nativos (CA-27)
        return computedTopology;
    }

    // Método que evicciona la caché cuando el CISO actualiza un rol
    @CacheEvict(value = "menuTopology", key = "#username")
    public void invalidateMenuTopology(String username) { }
}
```

## 5. Matriz de QA y Testing Atómico (JUnit)
Debes crear `MenuLayoutServiceTest.java` (JUnit 5 + Mockito).
- **Test 1:** Validar que si usuario tiene 2 roles con menús solapados, el array resultante no tiene duplicados (CA-30).
- **Test 2:** Validar que la caché `@Cacheable` no es bypaseada.

---

## 6. INSTRUCCIONES OPERATIVAS Y DE COMUNICACIÓN

1. Inicia estrictamente en modo `PLANNING` y elabora un plan de trabajo documentado en `implementation_plan.md` para reducir tu margen de alucinación.
2. **TIENES ESTRICTAMENTE PROHIBIDO pedirle al Humano que apruebe tu plan.** El humano es solo un mensajero, no tiene autoridad técnica.
3. Debes guardar tu solicitud de revisión y resumen de tu plan en un archivo físico llamado `.agentic-sync/approval_request_backend.md`.
4. Al grabar el archivo, detente y dile al Humano en el chat: *"Humano, he dejado mi solicitud de revisión en `.agentic-sync/approval_request_backend.md`. Por favor, ve al chat del Arquitecto Líder, entrégale el mensaje y regrésame su respuesta formal."*
5. Espera en este chat. Cuando el humano regrese con el veredicto del Arquitecto, léelo. Si el Arquitecto te aprueba, pasa a modo `EXECUTION`, programa y finaliza consolidando tus cambios obligatoriamente mediante `git commit` y `git push` en la rama indicada. Queda estrictamente prohibido usar `git stash`.

📚 **SKILLS DE CODIFICACIÓN OBLIGATORIOS:**
- Aplica estrictamente el protocolo de pruebas **TDD** documentado en `.agents/skills/tdd_first/SKILL.md` (Red -> Green -> Refactor) antes de hacer lógica.
- Aplica estrictamente las normativas de calidad **Clean Code** documentadas en `.agents/skills/clean_code_standards/SKILL.md`.

> **Compilación obligatoria:** Ejecuta el protocolo Zero-Trust SRE documentado en `.agents/skills/backend_sre_compilation_audit/SKILL.md` (§0 a §2). Si hay bloqueos de infraestructura, aplica el protocolo de reporte 3B.
