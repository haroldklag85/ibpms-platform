# Guía Didáctica: Entendiendo el Nivel 2 del Modelo C4 (Diagrama de Contenedores)

**Autor:** Arquitecto de Software (Rol Profesor)
**Audiencia:** Desarrolladores, Product Owners, y Onboarding de nuevos miembros del equipo.
**Contexto:** Explicación "para dummies" del diagrama `C4 Container` (Nivel 2) alojado en `c4-model.md`.

---

## 🏭 ¿Qué estamos viendo aquí? La Fábrica Vista desde Arriba

Si recuerdas nuestras clases del **Modelo C4**:
- El Nivel 1 era el mapa de la ciudad entera.
- El Nivel 3 (nuestra guía anterior) era el plano minucioso de las oficinas dentro de nuestro edificio administrativo.

**El Nivel 2 (Diagrama de Contenedores)** es equivalente a ver todos los edificios que componen nuestro campus corporativo. No vemos los planos de los pisos, sino **los edificios independientes**, para qué sirven y cómo se mandan cajas (datos) unos a otros.

En términos de software, un "Contenedor" en este diagrama no significa *Docker*. Significa cualquier pieza de software que se despliegue y ejecute como una unidad separada (una Base de Datos, un Microservicio, un Servidor Web).

Nuestra Arquitectura V1 está basada en **Máquinas Virtuales en Azure (VNet)**. Vamos a recorrer el campus:

---

### 🛡️ 1. Las Puertas de Seguridad y el Perímetro

*   **Azure APIM (Gateway Facade):** Esta es la garita de seguridad principal de nuestra empresa. **Nadie** del mundo exterior puede hablar directamente con nuestros edificios internos. Si el usuario *(Frontend)*, o Microsoft *(O365)* quieren algo, tienen que tocarle la puerta al APIM. Este portero verifica tu llave (Token JWT), anota que entraste y te dirige al edificio correcto. Esto evita que los "hackers" ataquen nuestras máquinas virtuales directamente.

---

### 🏢 2. El Edificio Comercial: Frontend y Analítica

*   **Frontend SPA (Vue 3 / Vite):** Este es nuestro mostrador comercial. Es la aplicación web que descargamos en el navegador (Chrome/Edge) del usuario. Desde aquí, los empleados ven sus bandejas de entrada, llenan los formularios y reclaman tareas.
*   **Plugin Outlook:** Es una sucursal pequeña que pusimos directamente *adentro* del correo del usuario. En lugar de venir a nuestra página web, el usuario da clic en un botón de su Microsoft Outlook y aparece nuestro sistema.
*   **Dashboard BAM (Grafana):** Este es el cuarto de pantallas de control. Es un servidor analítico que lee a qué velocidad estamos procesando los expedientes. Como queremos que el usuario lo vea sin salir de su sesión, usamos un ventanal virtual (un "Iframe") para incrustarlo dentro de nuestro Frontend SPA.

---

### 🏭 3. La Fábrica Principal: El Backend Monolítico

Este es el galpón más grande y pesado. Le llamamos *Monolítico* porque, por ahora (V1), metimos tres líneas de ensamblaje diferentes dentro del mismo bloque de concreto (nuestra VM Java Spring Boot):

1.  **API Backend Core (REST/Java):** El director de la orquesta. Recibe las llamadas del APIM, orquesta la seguridad, y decide a quién pasarle la pelota.
2.  **Motor BPM/DMN Empotrado (Camunda 7 .jar):** Este es un robot pre-comprado (Open Source) que metimos en nuestra fábrica para que maneje los "flujos de trabajo". En lugar de programar nosotros el comportamiento del "aprobado" y "rechazado", el robot lee un mapa visual (BPMN) y mueve las piezas por nosotros.
3.  **Generador Docs Oficiales (FOP):** Literalmente la imprenta. Cuando el director le dice "el crédito fue aprobado", toma una plantilla, le inyecta el JSON de datos y escupe un PDF legal, inmutable, listo para almacenamiento.

---

### 🧠 4. El Cuarto Satélite de Ciencias: La IA Perimetral

*   **Motor IA Perimetral (Llama 3 / vLLM):** En lugar de mandar nuestros secretos a ChatGPT (por fuera), pusimos un servidor propio muy potente en nuestra red privada con un cerebro artificial. Nuestro Backend le manda texto humano ("Crea una regla que exija 18 años para pedir crédito") y este cuarto nos devuelve código de tabla de decisiones (DMN).

---

### 🏦 5. Las Bóvedas Subterráneas: Capa de Persistencia

Los edificios de arriba se apagan y pierden la memoria. Aquí es donde guardamos las cosas para la eternidad:

*   **MySQL 8 (Base de Datos Consolidada):** Es el archivador maestro. Aquí vive todo: nuestros datos de negocio puros (`ibpms_case`) y las tablas operativas de nuestro robot Camunda (`ACT_RU_TASK`).  Con el parche de **Transaccionalidad** que hicimos en la otra clase, aseguramos que si un archivo se quema, se anula toda la transacción para ambos lados (ACID).
*   **Azure Managed Disks (Bóveda física):** A MySQL le hace daño guardar archivos PDF pesados. Por eso creamos una caja fuerte separada solo para binarios. Nuestro sistema core guarda en MySQL un código criptográfico (Hash SHA-256) y un mapa hacia donde está el disco duro que tiene el PDF final. A esto le llamamos **El SGDEA**.

---

### 🌐 6. El Ecosistema Cercano (Sistemas Externos - Integraciones)

*   **MS Graph (Webhooks de O365):** Imagina que MS Graph es la cartera de correos externa. En vez de que nosotros tengamos que ir a preguntar cada 5 minutos *"¿Llegó carta nueva?"*, activamos un "Webhook". Es decir, MS Graph nos marca rápidamente al APIM diciendo: *"¡Acaba de caer un correo en la bandeja legal, recojanlo!"*, despertando a nuestra fábrica automáticamente.

---

### 🎓 Moraleja del Profesor: El Viaje a la V2

Este diagrama V1 dibuja todo centralizado e hiper-conectado a nuestro Backend Monolítico. **El desafío arquitectónico a futuro (Para la V2 SaaS)** consistirá en tomar las sierras y separar el robot Camunda, el generador de PDFs y la IA en **sus propios edificios flotantes (Contenedores Kubernetes)**, reemplazando las tuberías rígidas actuales (REST síncrono) por mensajeros en motocicletas (Event-Driven / Apache Kafka) para que nuestra ciudad pueda soportar miles de inquilinos diferentes al mismo tiempo.

¡Estúdiate este mapa táctico porque será fundamental antes de sentarnos a tirar código de configuración o levantar los entornos (`docker-compose`) localmente con el equipo!
