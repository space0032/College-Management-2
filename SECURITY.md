# Security Policy

## Supported Versions

Currently, only the latest version of the College Management System is supported with security updates.

## Security Best Practices

### 1. Credential Management
- **Never commit** `.env` or `CREDENTIALS.txt` files to version control
- Always use `.env.example` as a template and create your own `.env` file
- Change default passwords immediately after installation
- Use strong, unique passwords for all user accounts

### 2. Database Security
- Run the database with a dedicated user account (not root)
- Use strong passwords for database users
- Limit database access to localhost only in production
- Regularly backup your database

### 3. Password Security
- All passwords are hashed using SHA-256
- New installations can use salted hashing by enabling it in `PasswordUtils`
- Change passwords regularly
- Enforce password complexity requirements

### 4. Network Security
- Run the application behind a firewall
- Use HTTPS/SSL when deploying in production
- Limit API access to trusted networks only

### 5. Regular Updates
- Keep Java and MySQL updated to the latest versions
- Monitor dependencies for security vulnerabilities
- Apply security patches promptly

## Reporting a Vulnerability

If you discover a security vulnerability in this project, please report it by:

1. **DO NOT** create a public GitHub issue
2. Email the maintainer with details about the vulnerability
3. Include steps to reproduce the issue
4. Allow reasonable time for a fix to be developed

We take security seriously and will respond to valid reports promptly.

## Known Security Considerations

### Legacy Password Hashing
- Existing installations use SHA-256 without salt for backward compatibility
- This is considered weak by modern standards
- **Recommendation**: Plan a migration to use salted hashing for new passwords
- Future versions will enforce stronger password hashing

### Database Connection Management
- The application creates new database connections per request
- Consider implementing connection pooling for production deployments
- Monitor connection usage to prevent resource exhaustion

### Input Validation
- All user inputs should be validated before processing
- SQL injection protection is implemented via PreparedStatements
- Additional validation layers should be added at the application level

## Security Checklist for Deployment

- [ ] Changed all default passwords
- [ ] Created `.env` file with production credentials
- [ ] Verified `.env` is in `.gitignore`
- [ ] Removed CREDENTIALS.txt or secured it appropriately
- [ ] Database is running with a non-root user
- [ ] Database access is restricted to localhost
- [ ] Application is running behind a firewall
- [ ] Regular backups are configured
- [ ] Logging is enabled and monitored
- [ ] SSL/TLS is configured if exposed to network

## Contact

For security concerns, please contact the repository maintainers.
