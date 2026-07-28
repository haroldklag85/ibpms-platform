import { execSync } from 'child_process';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
async function globalTeardown() {
  console.log('\n[Playwright] Global Teardown: Destruyendo contenedores E2E...');
  try {
    const composeFile = path.resolve(__dirname, '../../docker-compose.e2e.yml');
    execSync(`docker compose -f "${composeFile}" down -v --remove-orphans`, {
      stdio: 'inherit',
    });
    console.log('[Playwright] Contenedores E2E destruidos con éxito.');
  } catch (error) {
    console.error('[Playwright] Error al destruir contenedores E2E:', error);
  }
}

export default globalTeardown;
