import { Page } from '@playwright/test';

export async function loginE2E(page: Page, role: 'CISO' | 'BUSINESS' | 'AGENT' | 'EXECUTIVE' = 'BUSINESS') {
    // Navigate to local or deployed frontend
    await page.goto('/login');
    
    // Choose appropriate UAT credential based on Zero-Trust Matrix
    // The backend seed data contains 'ibpms_ciso', 'ibpms_clerk', etc.
    let username = '';
    
    switch (role) {
        case 'CISO':
            username = 'ibpms_admin'; // Based on US-048 / US-011
            break;
        case 'AGENT':
            username = 'ibpms_clerk';
            break;
        case 'EXECUTIVE':
            username = 'ibpms_executive';
            break;
        case 'BUSINESS':
        default:
            username = 'ibpms_business';
            break;
    }

    await page.fill('input[type="email"]', `${username}@ibpms.com`);
    await page.fill('input[type="password"]', 'P@ssw0rd123!');
    
    // Check Terms
    const termsCheckbox = page.locator('input[type="checkbox"]');
    if (await termsCheckbox.isVisible()) {
        await termsCheckbox.check();
    }

    // Submit
    await Promise.all([
        page.waitForURL('**/workdesk'),
        page.click('button[type="submit"]')
    ]);

    // Verify successful login by checking for the global sidebar
    await page.waitForSelector('nav.sidebar', { state: 'visible' });
}
