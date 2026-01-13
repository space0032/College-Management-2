CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value TEXT
);

INSERT INTO permissions (code, name, category, description) 
SELECT 'MANAGE_COLLEGE_INFO', 'Manage College Info', 'System', 'Allow editing college name and logo'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'MANAGE_COLLEGE_INFO');

-- Insert default values if not exist
INSERT INTO system_settings (setting_key, setting_value)
VALUES ('COLLEGE_NAME', 'College Manager')
ON CONFLICT (setting_key) DO NOTHING;

INSERT INTO system_settings (setting_key, setting_value)
VALUES ('COLLEGE_LOGO_PATH', '')
ON CONFLICT (setting_key) DO NOTHING;
