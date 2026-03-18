const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// 1. Cargar .env de forma nativa (sin dependencias npm externas)
function loadEnv() {
    const envPath = path.join(__dirname, '.env');
    if (!fs.existsSync(envPath)) return false;
    
    const content = fs.readFileSync(envPath, 'utf-8');
    content.split('\n').forEach(line => {
        const match = line.match(/^\s*([\w.-]+)\s*=\s*(.*)/);
        if (match) {
            let val = match[2].trim();
            // Remover comillas si existen
            if (val.startsWith('"') && val.endsWith('"')) val = val.slice(1, -1);
            if (val.startsWith("'") && val.endsWith("'")) val = val.slice(1, -1);
            process.env[match[1]] = val;
        }
    });
    return true;
}

if (!loadEnv() || !process.env.OPENAI_API_KEY) {
    console.error("❌ ERROR CRÍTICO: No se encontró el archivo .env o la variable OPENAI_API_KEY en .agent/scripts/.env");
    console.error("Por favor, renombra .env.example a .env y coloca tu llave.");
    process.exit(1);
}

// 2. Parsear Argumentos CLI
const args = process.argv.slice(2);
const roleArg = args.find(a => a.startsWith('--role='));
const fileArg = args.find(a => a.startsWith('--file='));

if (!roleArg || !fileArg) {
    console.error("Uso incorrecto. Formato: node subagent.js --role=[backend|frontend] --file=[ruta_al_handoff.md]");
    process.exit(1);
}

const role = roleArg.split('=')[1];
const handoffPath = fileArg.split('=')[1];
const projectRoot = path.resolve(__dirname, '../../'); // Subimos fuera de .agent/scripts

try {
    const handoffContent = fs.readFileSync(path.resolve(projectRoot, handoffPath), 'utf-8');
    
    console.log(`\n========================================================`);
    console.log(`🤖 Levantando Agente Especialista [${role.toUpperCase()}] en Background`);
    console.log(`📂 Contrato: ${handoffPath}`);
    console.log(`========================================================\n`);

    // 3. Construir el Prompt del Sistema "Blindado"
    const systemPrompt = `
Eres un Agente Especialista Senior actuando estrictamente bajo el rol de: ${role}.
Estás operando en un entorno automatizado (Swarm CLI framework). TU ÚNICA FUNCIÓN es procesar el plan que se te entregará a continuación (El Handoff) y redactar el código exacto necesario para implementarlo.

REGLAS DE ORO IMBORRABLES:
1. TIENES PROHIBIDO "hablar", saludar o explicar tu código en la respuesta.
2. TU ÚNICA SALIDA VÁLIDA es devolver el contenido de los archivos modificados dentro de bloques XML exactos.
3. El formato obligatorio para CADA archivo modificado o creado es:

<file_update path="ruta/relativa/al/archivo.extension">
// Código fuente final y completo aquí. NUNCA uses "..." ni abrevies nada porque el código se sobreescribirá en disco entero.
</file_update>

4. Las rutas del XML deben ser relativas a la raíz del proyecto (Ej: frontend/src/views/admin/FormDesigner.vue).
5. No devuelvas NINGÚN texto fuera de las etiquetas XML <file_update>. Si necesitas más archivos, anida múltiples bloques <file_update>.
`;

    const requestBody = {
        model: "gpt-4o", // O modelo equivalente si se configura diferente
        messages: [
            { role: "system", content: systemPrompt },
            { role: "user", content: `Handoff Contract del Arquitecto:\n\n${handoffContent}` }
        ],
        temperature: 0.2 // Baja temperatura para código determinista
    };

    console.log("⏳ Conectando con el Motor Neuronal Central (OpenAI API)... (Esto puede tomar entre 10 y 45 segundos)");
    
    // 4. Invocar la API
    fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`
        },
        body: JSON.stringify(requestBody)
    }).then(async res => {
        if (!res.ok) {
            const errData = await res.text();
            console.error("❌ ERROR LLM HTTP:", res.status, errData);
            process.exit(1);
        }
        return res.json();
    }).then(data => {
        if (data.error) {
            console.error("❌ ERROR LLM PAYLOAD:", data.error.message);
            process.exit(1);
        }
        
        const answer = data.choices[0].message.content;
        
        // 5. Parseador Feroz de XML
        const regex = /<file_update path="([^"]+)">([\s\S]*?)<\/file_update>/g;
        let match;
        let filesUpdated = 0;
        
        while ((match = regex.exec(answer)) !== null) {
            let filePath = match[1].trim();
            const content = match[2];
            
            // Re-vincular al path absoluto de la raíz del proyecto
            const absolutePath = path.resolve(projectRoot, filePath);
            
            // Crear carpetas padre si no existen
            fs.mkdirSync(path.dirname(absolutePath), { recursive: true });
            
            // Escribir el arcivo fisicamente (Borrando espacios indeseados al inicio y fin generados por markdown escapes)
            let finalContent = content.trim();
            if(finalContent.startsWith('```') && finalContent.endsWith('```')) {
                // Limpiar tildes invertidas si el LLM se equivocó de formato
                const lines = finalContent.split('\n');
                lines.shift(); // remove opening ```lang
                lines.pop(); // remove closing ```
                finalContent = lines.join('\n');
            }
            
            fs.writeFileSync(absolutePath, finalContent);
            console.log(`✅ Archivo Mutilado/Escrito Exitosamente: ${filePath}`);
            filesUpdated++;
        }
        
        if (filesUpdated === 0) {
            console.warn("⚠️ ALERTA DE ALUCINACIÓN: El agente procesó el requerimiento pero no devolvió bloques <file_update> válidos.");
            console.warn("Respuesta cruda devuelta:");
            console.log(answer);
            process.exit(1);
        }
        
        // 6. Patrón Gatekeeper Automático
        const tzoffset = (new Date()).getTimezoneOffset() * 60000;
        const localISOTime = (new Date(Date.now() - tzoffset)).toISOString().slice(0, 16).replace('T', '_');
        const stashName = `temp-${role}-${localISOTime}`;
        
        console.log(`📦 Empaquetando capa de seguridad en Stash: "${stashName}"...`);
        try {
            execSync(`git add . && git stash save "${stashName}"`, { cwd: projectRoot });
            console.log(`\n🚀 [MISIÓN CUMPLIDA] Subagente apagándose. El Orquestador puede hacer 'git stash pop' ahora.\n`);
            process.exit(0);
        } catch(gitErr) {
            console.error("❌ ERROR AL HACER STASH:", gitErr.message);
            process.exit(1);
        }

    }).catch(err => {
        console.error("❌ FETCH CRASH ERROR:", err);
        process.exit(1);
    });

} catch (e) {
    console.error("❌ EXCEPCIÓN FATAL DEL DISCO:", e);
    process.exit(1);
}
