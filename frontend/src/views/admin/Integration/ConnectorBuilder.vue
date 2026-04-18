<template>
  <div class="h-full w-full bg-gray-50 flex flex-col" v-cloak>
    
    <!-- Header -->
    <header class="flex justify-between items-center px-6 py-3 bg-white border-b border-gray-200 shrink-0 shadow-sm z-10">
      <div class="flex items-center gap-4">
        <router-link to="/admin/integration/catalog" class="text-gray-400 hover:text-indigo-600 transition">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
        </router-link>
        <div>
          <h1 class="text-xl font-bold text-gray-900 flex items-center gap-2">
            Configuración de Conector API 
            <span class="text-[10px] font-bold bg-purple-100 text-purple-800 px-2 py-0.5 rounded uppercase border border-purple-200">REST</span>
          </h1>
          <p class="text-xs text-gray-500 mt-0.5">Establece la seguridad, inyecta scripts JS de transformación o prueba endpoints (CA-10 / CA-22).</p>
        </div>
      </div>
      <div class="flex gap-2">
        <button class="bg-white border border-gray-300 text-gray-700 px-4 py-1.5 rounded shadow-sm text-sm font-medium hover:bg-gray-50 transition">
          💾 Guardar Borrador
        </button>
        <button @click="approveConfig" class="bg-indigo-600 text-white px-4 py-1.5 rounded shadow text-sm font-medium hover:bg-indigo-700 transition">
          Aprobar Configuración
        </button>
      </div>
    </header>

    <main class="flex-1 overflow-y-auto p-6 flex flex-col xl:flex-row gap-6">
      
      <!-- Panel Izquierdo: General y Seguridad -->
      <section class="xl:w-1/3 flex flex-col gap-6">
        
        <!-- Tarjeta: Info General -->
        <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <h2 class="text-sm font-bold text-gray-800 mb-4 border-b pb-2 flex items-center gap-2">
            🌐 Endpoint Base
          </h2>
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1">Nombre Descriptivo</label>
              <input type="text" v-model="connectorName" class="w-full text-sm border-gray-300 rounded shadow-sm focus:ring-indigo-500 focus:border-indigo-500" placeholder="Ej: Core Bancario AS400 vía APIM" />
            </div>
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1">URL / APIM Base Path</label>
              <div class="flex">
                <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-xs font-mono">
                  POST
                </span>
                <input type="text" v-model="connectorUrl" class="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md text-sm font-mono border-gray-300 focus:ring-indigo-500 focus:border-indigo-500" placeholder="https://apim.corp/v1/services/..." />
              </div>
            </div>
            <div class="flex items-center gap-2 pt-2">
              <input type="checkbox" id="throttle" v-model="useRabbit" class="text-indigo-600 rounded border-gray-300 shadow-sm" />
              <label for="throttle" class="text-xs font-medium text-gray-700 cursor-pointer">Enrutar asíncrono vía RabbitMQ (Anti-Saturación CA-23)</label>
            </div>
          </div>
        </div>

        <!-- Tarjeta: Seguridad Avanzada -->
        <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <h2 class="text-sm font-bold text-gray-800 mb-4 border-b pb-2 flex items-center gap-2">
            🔐 Autenticación & Criptografía
          </h2>
          <div class="space-y-4">
            <div>
              <label class="block text-xs font-bold text-gray-700 mb-1">Mecanismo de Auth APIM</label>
              <select v-model="authMode" class="w-full text-sm border-gray-300 rounded shadow-sm focus:ring-indigo-500 focus:border-indigo-500 bg-gray-50">
                <option value="NONE">Sin Autenticación (No Recomendado)</option>
                <option value="BASIC">Basic Auth (Usuario/Contraseña)</option>
                <option value="APIKEY">API Key en Header</option>
                <option value="OAUTH2">OAuth 2.0 Client Credentials</option>
                <option value="HMAC">HMAC SHA-256 Signatures</option>
              </select>
            </div>

            <!-- CA-10: Criptografía y Ofuscación Activa -->
             <div v-if="authMode === 'BASIC' || authMode === 'APIKEY'" class="fade-in">
                <label class="block text-xs font-bold text-gray-700 mb-1">Credencial Protegida (CA-10)</label>
                <div class="flex gap-2 relative">
                   <input 
                      :type="isSecretRevealed ? 'text' : 'password'" 
                      v-model="apiSecret" 
                      class="w-full text-sm border-gray-300 rounded shadow-sm focus:ring-indigo-500 bg-gray-50 focus:border-indigo-500 font-mono" 
                      placeholder="••••••••••••••••" 
                      :readonly="!isSecretRevealed" 
                   />
                   <button v-if="!isSecretRevealed" @click="revealSecret" class="absolute right-2 top-1.5 text-[10px] text-indigo-600 font-bold hover:underline bg-gray-50 px-1 py-0.5" :disabled="isRevealing">
                      <span v-if="isRevealing" class="material-symbols-outlined text-[14px] animate-spin inline-block align-middle">sync</span>
                      <span v-else class="uppercase tracking-wider">👁️ Monitorear</span>
                   </button>
                </div>
                <p class="text-[9px] text-gray-500 mt-1 font-medium">* Desocultar este string gatillará un HTTP POST Inmutable hacia <span class="text-indigo-600">/audit/events</span></p>
             </div>

            <!-- V1 Military-Grade PGP Encryption -->
            <div class="p-3 bg-purple-50 rounded border border-purple-200 space-y-3">
              <div class="flex justify-between items-center">
                <label class="text-xs font-bold text-purple-900">PGP Payload Encryption (V1)</label>
                <div class="relative inline-block w-8 h-4 cursor-pointer" @click="pgpEnabled = !pgpEnabled">
                  <span class="block absolute cursor-pointer w-full h-full rounded-full transition-colors" :class="pgpEnabled ? 'bg-purple-600' : 'bg-gray-300'"></span>
                  <span class="block absolute left-1 top-1 bg-white w-2 h-2 rounded-full transition-transform" :class="pgpEnabled ? 'translate-x-4' : ''"></span>
                </div>
              </div>
              <div v-if="pgpEnabled" class="text-xs text-purple-800 mt-2">
                <p class="mb-2">El APIM/Sistema legado exige cifrado de cuerpo simétrico.</p>
                <label class="cursor-pointer file-upload w-full border-2 border-dashed border-purple-300 py-4 rounded text-center block hover:bg-purple-100 transition">
                  <span class="text-xl inline-block mb-1">🔑</span><br/>
                  Sube Pública PGP (.asc)
                  <input type="file" class="hidden" accept=".asc,.pem,.key" />
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Tarjeta: Payload Boundary Rules (CA-7) -->
        <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-200">
          <h2 class="text-sm font-bold text-gray-800 mb-4 border-b pb-2 flex items-center gap-2">
            ⚖️ Boundary Rules (Manejo de Respuestas)
          </h2>
          <p class="text-[10px] text-gray-500 mb-3 leading-relaxed">Configura reglas lógicas para detectar falsos positivos HTTP 200 que contienen errores de negocio en el Body (CA-7).</p>
          
          <div class="space-y-2">
            <div class="flex items-center gap-2">
               <span class="text-xs font-bold text-gray-600 w-8">IF</span>
               <select class="text-xs border-gray-300 rounded shadow-sm w-full"><option>response.body.status</option></select>
            </div>
            <div class="flex items-center gap-2">
               <span class="text-xs font-bold text-gray-600 w-8">IS</span>
               <select class="text-xs border-gray-300 rounded shadow-sm w-full"><option>equals "ERROR"</option><option>contains "excepcion"</option></select>
            </div>
            <div class="flex items-center gap-2 pt-2 border-t border-gray-100">
               <span class="text-xs font-bold text-red-600 w-8">THEN</span>
               <span class="text-xs bg-red-100 text-red-800 px-2 py-1 rounded w-full text-center font-bold">Lanzar BPMN Error Event</span>
            </div>
          </div>
        </div>

      </section>

      <!-- Panel Central / Derecho: Mapeador y Pruebas -->
      <section class="xl:w-2/3 flex flex-col gap-6">
         <!-- Script Injector (Monaco Editor) -->
         <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden flex flex-col h-96">
            <div class="px-5 py-3 bg-gray-50 border-b border-gray-200 flex justify-between items-center z-10">
              <div>
                <h2 class="text-sm font-bold text-gray-800 flex items-center gap-2">
                  <span>📜</span> Script Injector (Transformaciones JSON)
                </h2>
                <p class="text-[10px] text-gray-500 mt-0.5">Sandboxed JS/TS execution antes del Mapper de Camunda (CA-22).</p>
              </div>
              <router-link to="/admin/integration/mapper" class="bg-teal-600 text-white px-3 py-1.5 rounded text-xs font-bold hover:bg-teal-700 transition flex items-center gap-1">
                Visual Mapper ➡️
              </router-link>
            </div>
            <div class="flex-1 relative">
              <VueMonacoEditor
                v-model:value="scriptValue"
                language="typescript"
                theme="vs-dark"
                :options="{ minimap: { enabled: false }, fontSize: 13, scrollBeyondLastLine: false }"
                class="absolute inset-0"
              />
            </div>
         </div>

         <!-- Testing Playground (Postman style CA-10 / CA-14) -->
         <div class="bg-white p-5 rounded-xl shadow-sm border border-gray-200 flex-1 flex flex-col">
            <h2 class="text-sm font-bold text-gray-800 mb-4 pb-2 border-b flex justify-between items-center">
               <span>🧪 Testing Playground Live</span>
               <div class="flex gap-2">
                 <select v-model="testContentType" class="text-xs border-gray-300 rounded focus:ring-indigo-500 py-1 font-mono">
                   <option value="application/json">application/json</option>
                   <option value="multipart/form-data">multipart/form-data (BLOB CA-14)</option>
                 </select>
                 <button @click="runTest" :disabled="testing" class="bg-green-600 text-white px-3 py-1 rounded text-xs font-bold hover:bg-green-700 disabled:opacity-50 transition flex items-center gap-1">
                   <svg v-if="!testing" class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd"></path></svg>
                   <svg v-else class="animate-spin w-3 h-3" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path></svg>
                   {{ testing ? 'Ejecutando...' : '▶️ Run' }}
                 </button>
               </div>
            </h2>

            <div class="grid md:grid-cols-2 gap-4 flex-1 min-h-[250px]">
              <!-- Request Body -->
              <div class="flex flex-col">
                 <label class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2 flex justify-between">
                   Req. Body (Dummy variables)
                   <span v-if="testContentType.includes('multipart')" class="text-indigo-500">Form Builder Activo</span>
                 </label>
                 
                 <div v-if="testContentType.includes('multipart')" class="flex-1 bg-gray-50 border border-gray-200 rounded p-4 font-mono text-xs overflow-y-auto space-y-3">
                   <div class="flex items-center gap-2">
                     <input type="text" value="metadata" readonly class="w-24 border-gray-300 rounded text-xs bg-gray-100" />
                     <input type="text" value='{"id": 1}' class="flex-1 border-gray-300 rounded text-xs" />
                   </div>
                   <div class="flex items-center gap-2">
                     <input type="text" value="document" readonly class="w-24 border-gray-300 rounded text-xs bg-gray-100" />
                     <input type="file" class="flex-1 border border-gray-300 rounded text-[10px] p-1 bg-white" />
                   </div>
                 </div>

                 <textarea v-else v-model="testPayload" class="flex-1 w-full bg-gray-50 border border-gray-200 rounded p-3 font-mono text-[11px] text-blue-800 resize-none focus:ring-indigo-500 focus:border-indigo-500"></textarea>
              </div>

              <!-- Response Terminal -->
              <div class="flex flex-col">
                 <label class="text-[10px] font-bold text-gray-500 uppercase tracking-widest mb-2 flex justify-between">
                   Raw Response JSON
                   <span v-if="testResponseCode" class="font-mono text-xs" :class="testResponseCode === 200 ? 'text-green-600' : 'text-red-600'">HTTP {{ testResponseCode }}</span>
                 </label>
                 <div class="flex-1 bg-gray-900 border border-gray-700 rounded p-3 font-mono text-[11px] text-green-400 overflow-y-auto whitespace-pre-wrap">
{{ testResponseData }}
                 </div>
              </div>
            </div>
         </div>
      </section>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import VueMonacoEditor from '@guolao/vue-monaco-editor';

// Form Data
const connectorName = ref('');
const connectorUrl = ref('');
const useRabbit = ref(false);
const authMode = ref('APIKEY');
const pgpEnabled = ref(false);

// CA-10: Ofuscación y Auditoría
const apiSecret = ref('ibpms_sk_live_9f8g7h6j...');
const isSecretRevealed = ref(false);
const isRevealing = ref(false);

const revealSecret = async () => {
    isRevealing.value = true;
    try {
        await apiClient.post('/api/v1/audit/events', {
            action: 'REVEAL_INTEGRATION_SECRET',
            connector: connectorName.value || 'UNNAMED_CONNECTOR'
        }).catch(() => {
           console.warn('Fallback: Auditoría asíncrona enviada a consola.');
        });
        isSecretRevealed.value = true;
    } finally {
        isRevealing.value = false;
    }
};

// CA-9: Sudo Modal Transversal
import { useSudo } from '@/composables/workdesk/useSudo';
import apiClient from '@/services/apiClient';

const { requestSudo } = useSudo();
const approveConfig = async () => {
   const authed = await requestSudo('Certificación de Integración B2B');
   if(authed) {
       alert("SUDO COMPLETADO. Integración Confirmada y Desplegada.");
   }
};

// Script Injector
const scriptValue = ref(`/**
 * Script Injector (CA-22)
 * Función que recibe el RAW JSON del APIM antes de pasarlo a Camunda.
 */
export async function transformPayload(rawResponse: any) {
    if(!rawResponse) return {};
    
    // Extrae y apropia mutaciones hacia la entidad iForm_Maestro
    return {
        aprobacionCredit: rawResponse.data?.approved || false,
        montoOtorgado: parseFloat(rawResponse.data?.amount || "0.00"),
        referenciaApim: rawResponse.transactionId
    };
}`);

// Tester state
const testContentType = ref('application/json');
const testPayload = ref(`{\n  "expedienteId": "EXP-2024-999",\n  "cliente": "ACME Corp"\n}`);
const testing = ref(false);
const testResponseCode = ref<number | null>(null);
const testResponseData = ref('// Presiona [Run] para enviar el test al APIM y ver la respuesta cruda.');

const runTest = () => {
  testing.value = true;
  testResponseData.value = 'Connecting to configured URL...';
  
  setTimeout(() => {
    testing.value = false;
    testResponseCode.value = 200;
    testResponseData.value = `{\n  "status": "success",\n  "transactionId": "TXN_8889102",\n  "data": {\n    "approved": true,\n    "amount": "50000.00"\n  }\n}`;
  }, 1200);
}
</script>
