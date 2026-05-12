--liquibase formatted sql
--changeset Antigravity:us029-seed-qa-form context:dev,test
--comment US-029: Seed data para validacion dinamica del motor de formularios (OBS-QA-01)

-- 1. Insert into ibpms_form_design
INSERT INTO ibpms_form_design (id, name, technical_name, pattern, status, version, vue_template, zod_schema, author_id, created_at, updated_at)
VALUES (
    'f0000000-0000-4000-8000-000000000001',
    'Complex QA Form',
    'qa_form_complex_schema',
    'SIMPLE',
    'ACTIVE',
    1,
    '<template><div>QA Form</div></template>',
    'z.object({})',
    'qa_system',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 2. Insert into ibpms_form_definitions
INSERT INTO ibpms_form_definitions (id, form_id, version_id, schema_content, created_by, created_at, hash_sha256)
VALUES (
    'd0000000-0000-4000-8000-000000000001',
    'f0000000-0000-4000-8000-000000000001',
    1,
    '{
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {
        "applicantName": {
          "type": "string",
          "minLength": 3
        },
        "applicantAge": {
          "type": "integer",
          "minimum": 18
        },
        "hasInsurance": {
          "type": "boolean"
        }
      },
      "required": ["applicantName", "applicantAge", "hasInsurance"]
    }'::jsonb,
    'qa_system',
    NOW(),
    'c2c1a82f3a4b953d5a452ef76e331c518b6e3f3b9cd4b5c71d6f21226b911246'
) ON CONFLICT DO NOTHING;

-- 3. Insert into ibpms_form_certifications
INSERT INTO ibpms_form_certifications (id, form_definition_id, is_qa_certified, certified_schema_hash, certified_by, certified_at)
VALUES (
    'c0000000-0000-4000-8000-000000000001',
    'd0000000-0000-4000-8000-000000000001',
    TRUE,
    'c2c1a82f3a4b953d5a452ef76e331c518b6e3f3b9cd4b5c71d6f21226b911246',
    'qa_system',
    NOW()
) ON CONFLICT DO NOTHING;
