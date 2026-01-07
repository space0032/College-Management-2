-- ============================================================================
-- FIX ALL LOGIN AND CONFIGURATION ISSUES
-- ============================================================================
-- This script fixes:
-- 1. Password hashes (SHA-256) for all users
-- 2. Audit log foreign key constraints
-- ============================================================================

USE college_management;

-- ============================================================================
-- 1. FIX PASSWORD HASHES
-- ============================================================================
-- The Java app uses SHA-256 hashing. Update all passwords to correct hashes.

-- Admin password: admin123
-- Hash: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
UPDATE users SET password = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'
WHERE username = 'admin';

-- Faculty password: faculty123  
-- Hash: 27041f5856c7387a997252694afb048d1aa939228ffcdbd6285b979b8da20e7a
UPDATE users SET password = '27041f5856c7387a997252694afb048d1aa939228ffcdbd6285b979b8da20e7a'
WHERE role IN ('FACULTY', 'WARDEN');

-- Student password: student123
-- Hash: 703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b
UPDATE users SET password = '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b'
WHERE role = 'STUDENT';

SELECT 'Password hashes updated successfully!' as Status;

-- ============================================================================
-- 2. FIX AUDIT LOG CONSTRAINTS
-- ============================================================================
-- Make user_id nullable to allow logging of failed login attempts

ALTER TABLE audit_logs MODIFY COLUMN user_id INT NULL;

SELECT 'Audit log constraints fixed!' as Status;

-- ============================================================================
-- 3. VERIFICATION
-- ============================================================================
SELECT 
    role,
    COUNT(*) as user_count,
    CASE 
        WHEN role = 'ADMIN' THEN 'admin123 (hash: 240be...)'
        WHEN role IN ('FACULTY', 'WARDEN') THEN 'faculty123 (hash: 27041f...)'
        WHEN role = 'STUDENT' THEN 'student123 (hash: 703b0a...)'
    END as password_info
FROM users 
GROUP BY role
ORDER BY role;

SELECT 'All fixes applied successfully! You can now login.' as Final_Status;
