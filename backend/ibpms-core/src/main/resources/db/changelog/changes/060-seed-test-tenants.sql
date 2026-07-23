-- liquibase formatted sql
-- changeset antigravity:060-seed-test-tenants context:dev,test

-- Insert test-specific tenants to resolve foreign key constraints on integration tests
INSERT INTO ibpms_tenant (slug, name, is_active)
VALUES 
  ('tenant1', 'Test Tenant 1', true),
  ('sandbox_tenant', 'Sandbox Isolation Tenant', true)
ON CONFLICT (slug) DO NOTHING;
