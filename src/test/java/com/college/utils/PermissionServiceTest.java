package com.college.utils;

import com.college.models.Permission;
import com.college.models.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionServiceTest {

    @Test
    void administratorIsGrantedNewPermissionsWithoutAnExplicitMapping() {
        Role admin = new Role();
        admin.setCode("ADMIN");

        assertTrue(PermissionService.grantsPermission(admin, "CREATE_LIBRARY"));
    }

    @Test
    void regularRoleStillRequiresAnExplicitPermission() {
        Role faculty = new Role();
        faculty.setCode("FACULTY");
        Permission permission = new Permission();
        permission.setCode("VIEW_STUDENT");
        faculty.addPermission(permission);

        assertTrue(PermissionService.grantsPermission(faculty, "VIEW_STUDENT"));
        assertFalse(PermissionService.grantsPermission(faculty, "CREATE_STUDENT"));
    }

    @Test
    void secondaryRolePermissionsAreIncludedInTheEffectiveUnion() {
        Role primary = new Role();
        primary.setCode("STUDENT");
        Role secondary = new Role();
        secondary.setCode("WARDEN");

        Permission wardenPerm = new Permission();
        wardenPerm.setCode("MANAGE_HOSTEL");
        secondary.addPermission(wardenPerm);
        Permission studentPerm = new Permission();
        studentPerm.setCode("VIEW_OWN_FEES");
        primary.addPermission(studentPerm);

        // Secondary role grants its perks...
        assertTrue(PermissionService.grantsAnyPermission(java.util.List.of(primary, secondary), "MANAGE_HOSTEL"));
        // ...while the primary role keeps gating its own portal permissions.
        assertTrue(PermissionService.grantsAnyPermission(java.util.List.of(primary, secondary), "VIEW_OWN_FEES"));
        assertFalse(PermissionService.grantsAnyPermission(java.util.List.of(primary, secondary), "CREATE_STUDENT"));
    }

    @Test
    void emptyRoleSetNeverGrantsPermissions() {
        assertFalse(PermissionService.grantsAnyPermission(java.util.List.of(), "MANAGE_HOSTEL"));
    }
}
