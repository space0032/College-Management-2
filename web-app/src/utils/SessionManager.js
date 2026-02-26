/**
 * Centralized Session Management Utility
 * Handles user authentication state consistently across all pages
 */
class SessionManager {
  static USER_KEY = 'user';
  static TOKEN_KEY = 'token';

  /**
   * Get current user from localStorage with validation
   * @returns {Object|null} User object or null if not authenticated
   */
  static getUser() {
    try {
      const userStr = localStorage.getItem(this.USER_KEY);
      if (!userStr) return null;

      const user = JSON.parse(userStr);

      // Validate required fields
      if (!user.id || !user.role) {
        console.error('Invalid user object in localStorage');
        this.clearSession();
        return null;
      }

      return user;
    } catch (error) {
      console.error('Error parsing user from localStorage:', error);
      this.clearSession();
      return null;
    }
  }

  /**
   * Get user ID safely
   * @returns {number|null} User ID or null
   */
  static getUserId() {
    const user = this.getUser();
    return user ? user.id : null;
  }

  /**
   * Get user role safely
   * @returns {string|null} User role or null
   */
  static getUserRole() {
    const user = this.getUser();
    return user ? user.role : null;
  }

  /**
   * Check if user is authenticated
   * @returns {boolean}
   */
  static isAuthenticated() {
    return this.getUser() !== null;
  }

  /**
   * Save user session
   * @param {Object} user - User object with id and role
   * @param {string} token - JWT token
   */
  static setSession(user, token) {
    if (!user || !user.id || !user.role) {
      throw new Error(`Invalid user object: missing ${!user ? 'user' : !user.id ? 'id' : 'role'}`);
    }
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    if (token) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  /**
   * Clear user session
   */
  static clearSession() {
    localStorage.removeItem(this.USER_KEY);
    localStorage.removeItem(this.TOKEN_KEY);
  }

  /**
   * Get authentication token
   * @returns {string|null}
   */
  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if user has specific role
   * @param {string} role - Role to check
   * @returns {boolean}
   */
  static hasRole(role) {
    const userRole = this.getUserRole();
    return userRole === role;
  }

  /**
   * Check if user has any of the specified roles
   * @param {string[]} roles - Array of roles to check
   * @returns {boolean}
   */
  static hasAnyRole(roles) {
    const userRole = this.getUserRole();
    if (!userRole) return false;
    return roles.includes(userRole);
  }

  /**
   * Check if user has a specific permission
   * @param {string} permissionCode - Permission code to check (e.g. 'VIEW_STUDENTS')
   * @returns {boolean}
   */
  static hasPermission(permissionCode) {
    // Admin always gets full access for fallback safety
    if (this.hasRole('ADMIN')) return true;

    const user = this.getUser();
    if (!user || (!user.permissions && !Array.isArray(user.permissions))) {
      return false;
    }

    return user.permissions.some(p => p.code === permissionCode);
  }
}

export default SessionManager;
