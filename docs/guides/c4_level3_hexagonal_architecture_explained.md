# Guía Didáctica: Entendiendo el Nivel 3 del Modelo C4 (Arquitectura Hexagonal)

**Autor:** Arquitecto de Software (Rol Profesor)
**Audiencia:** Desarrolladores, Analistas y Onboarding de nuevos miembros del equipo.
**Contexto:** Explicación "para dummies" del diagrama `C4 Component` (Nivel 3) alojado en `c4-model.md`.

---

## 🗺️ ¿Qué estamos viendo aquí?

El **Modelo C4** tiene niveles como si fuera Google Maps. 
- El Nivel 1 te muestra el planeta Tierra (quién usa el sistema).
- El Nivel 2 te muestra los países (qué servidores y bases de datos hay).
- **El Nivel 3 aterriza directamente en tu ciudad y te muestra los edificios (las piezas de código o componentes que viven *adentro* de tu servidor Spring Boot)**.

Lo que estamos viendo en este diagrama es el interior de nuestra aplicación "Backend". Y para organizarla, usamos el famoso patrón de la **Arquitectura Hexagonal** (también llamada de Puertos y Adaptadores). 

Te lo explico capa por capa, desde la "calle" hacia el "corazón" del edificio:

---

### 🚪 1. El Borde Exterior: "Driving Adapters" (Los que nos tocan la puerta)
Imagina que nuestro sistema es un castillo cerrado. Los "Driving Adapters" son las ventanillas de atención al público. Son los únicos lugares por donde el mundo exterior puede pedirnos cosas.

*   **REST Controllers:** Es la ventanilla donde llega el *"Frontend Vue 3"* a decir: *"¡Oye, créame un expediente nuevo!"* o *"¡Dame mi lista de tareas!"*.
*   **O365 Webhook:** Es una ventanilla especial de correo. Aquí llega Microsoft a decir: *"¡Aviso! Acaba de entrar un correo nuevo con 3 PDFs adjuntos!"*.

Las ventanillas (Adapters) **no toman decisiones de negocio**. Son tontas. Su único trabajo es entender qué les están pidiendo (parsear el JSON) y pasarle la orden al interior del castillo.

---

### 🕴️ 2. La Capa de Coordinación: "Application UseCases" (Los Gerentes)
Las ventanillas le pasan los papeles a los gerentes. Estos gerentes ("Use Cases" o Casos de Uso) son los que coordinan el baile. Conocen la coreografía de nuestro negocio.

*   **CaseManagement UseCase:** Es el gerente principal de expedientes. Dice: *"A ver, me pidieron crear un caso. Primero tengo que validar si el usuario tiene permiso, luego armar el caso de negocio, y finalmente mandarlo a guardar"*.
*   **Security Policy (ABAC):** Es el gerente de seguridad (el grandulón de la puerta). Decide si saca de la fila a alguien porque *"No estás autorizado a ver los créditos de la Zona Sur"*.
*   **Feature Flags Service:** Es el nuevo gerente de operaciones tácticas. Él sabe si para un cliente en particular, una función específica está prendida o apagada (*"Al cliente A no le muestres el botón mágico de IA, pero al cliente B sí"*).
*   **Shared Transaction Manager (El Notario ACID):** ¡Este es vital! Es un notario público. Vigila que si el gerente de casos (`CaseManagement`) hace 5 cosas a la vez (por ejemplo: actualizar el expediente Y decirle al motor Camunda que avance), las 5 ocurran perfectas. Si una cosita pequeña falla en el paso 5, el notario grita *"¡Alerta! Falla general"*, echa el tiempo atrás (Roll-back) y deja la base de datos intacta. Es la regla "Todo o Nada" (Atomicidad).

---

### 💎 3. El Corazón Intocable: "Dominio Core" (La Bóveda)
Aquí reside la verdadera inteligencia de nuestra empresa. Es el centro del hexágono.

*   **Expediente / Proyecto (Java Pojo):** Son clases de Java puras. No saben de qué color es la pantalla, ni si usamos MySQL o si usamos Camunda. Aquí vive la regla de oro: *"Un Crédito no puede estar en estado COMPLETO si no tiene una suma aprobada mayor a Cero"*.
*   **¿Por qué está aislado?** Porque si mañana echamos a la basura MySQL o si sacamos a Camunda (en la V2), ¡nuestro corazón no se reescribe! El negocio se mantiene inmutable. Son las reglas puras de nuestra compañía.

---

### 🛵 4. El Borde Inferior: "Driven Adapters" (Los que hacen los mandados)
Así como tenemos ventanillas de entrada, tenemos puertas traseras por donde nuestros gerentes mandan a hacer la "talacha" (el lado físico, el hardware, las integraciones sucias). Todo está abstraído por interfaces.

*   **MySQL JPA Repositories:** El trabajador que guarda las cosas permanentemente en el disco duro.
*   **Camunda 7 API:** El engranaje del motor de flujos lógicos de BPM.
*   **Llama 3 Local Adapter:** El cadete que va, le pregunta al motor de Inteligencia Artificial (Llama 3) *"tradúceme este texto a reglas lógicas"* y trae la respuesta.
*   **Template Renderer (Apache FOP):** Literalmente la "impresora". Recoge los datos del caso, la plantilla y vomita un PDF contractual listo.
*   **ERP / SGDEA Outbounds:** El mensajero que se monta en su moto y va hasta el viejo sistema ERP corporativo (o al repositorio de documentos) a dejar la información final.

---

### 🤓 La lección final del Profesor: ¿Por qué las flechitas apuntan raras (Dependencia)?

Si miras el diagrama detenidamente, notarás algo extraño en las conexiones (flechas) hacia las capas de abajo (los mandaderos). La flecha no dice *"Usar el adaptador MySQL"*, la flecha dice **"Interface"** o **"Dependency Injection (DI)"**.

Esto es el secreto de la Arquitectura Hexagonal y se llama **Inversión de Dependencias (El Principio 'D' de SOLID)**.

*   En código espagueti normal (amateur), el Gerente ("Application UseCase") importa directamente el código de la Base de datos (`import com.mysql...`). Se vuelve esclavo de MySQL.
*   En nuestro diagrama de legos profesional, el Gerente grita: *"Oigan, yo solo sé hablar con un enchufe de forma redonda (Interface Repository). Alguien conécteme a alguien que tenga un enchufe redondo"*.
*   Y luego, el framework (Spring Boot) llega con un cable ("Inyección de Dependencia" o DI) y enchufa ahí silenciosamente el adaptador de "MySQL".

El Gerente jamás supo quién le guardó el dato. ¡Así es como podemos evolucionar de la V1 a la V2 sin derramar una lágrima! El dominio queda protegido de los cambios tecnológicos externos.
