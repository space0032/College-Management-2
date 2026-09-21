package com.college.dao;

import com.college.models.PayrollEntry;
import com.college.utils.ManagementException;
import com.college.utils.PayrollMigration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ManagementIntegrityTest {
    private String url;
    private PayrollDAO payroll;
    private AccessManagementDAO access;
    private Connection connection() throws SQLException { return DriverManager.getConnection(url); }
    private void sql(String sql) throws SQLException { try (Connection conn = connection(); Statement stmt = conn.createStatement()) { stmt.execute(sql); } }
    private int count(String sql) throws SQLException { try (Connection conn = connection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) { rs.next(); return rs.getInt(1); } }
    @BeforeEach void setup() throws Exception {
        url = "jdbc:h2:mem:management_" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=MONTH,YEAR;LOCK_TIMEOUT=10000";
        payroll = new PayrollDAO(this::connection); access = new AccessManagementDAO(this::connection);
        sql("CREATE TABLE employees (id INT PRIMARY KEY)");
        sql("INSERT INTO employees VALUES (1),(2)");
        sql("CREATE TABLE payroll_entries (id SERIAL PRIMARY KEY, employee_id INT REFERENCES employees(id), month INT, year INT, basic_salary DECIMAL(10,2), bonuses DECIMAL(10,2), deductions DECIMAL(10,2), net_salary DECIMAL(10,2), status VARCHAR(20), payment_date DATE)");
        try (Connection conn = connection()) { PayrollMigration.apply(conn); }
        sql("CREATE TABLE roles (id INT PRIMARY KEY, code VARCHAR(50) UNIQUE, is_system_role BOOLEAN)");
        sql("CREATE TABLE users (id INT PRIMARY KEY, role_id INT REFERENCES roles(id), role VARCHAR(50))");
        sql("CREATE TABLE role_permissions (role_id INT REFERENCES roles(id), permission_id INT)");
        sql("INSERT INTO roles VALUES (1,'ADMIN',TRUE),(2,'HR',FALSE),(3,'READER',FALSE),(4,'CUSTOM_SYSTEM',TRUE)");
        sql("INSERT INTO users VALUES (1,1,'ADMIN'),(2,2,'HR'),(3,3,'READER')");
        sql("INSERT INTO role_permissions VALUES (2,10),(2,20),(3,10)");
    }
    private PayrollEntry entry(int employee) { return new PayrollEntry(employee, 9, 2026, new BigDecimal("1234.56")); }
    @Test void concurrentGenerationCreatesExactlyOneRecord() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Integer> task = () -> { start.await(); return payroll.generateBatch(List.of(entry(1))).size(); };
            Future<Integer> first = executor.submit(task), second = executor.submit(task); start.countDown();
            assertEquals(1, first.get(15, TimeUnit.SECONDS) + second.get(15, TimeUnit.SECONDS));
            assertEquals(1, count("SELECT COUNT(*) FROM payroll_entries"));
        } finally { executor.shutdownNow(); }
    }
    @Test void invalidBatchRollsBackEarlierEntries() throws Exception {
        PayrollEntry invalid = entry(2); invalid.setDeductions(new BigDecimal("9999"));
        assertThrows(ManagementException.class, () -> payroll.generateBatch(List.of(entry(1), invalid)));
        assertEquals(0, count("SELECT COUNT(*) FROM payroll_entries"));
    }
    @Test void paidRowsRejectStaleAdjustmentsAndDeletion() throws Exception {
        payroll.createPayrollEntry(entry(1)); int id = count("SELECT id FROM payroll_entries");
        PayrollEntry stale = payroll.getById(id);
        assertTrue(payroll.markAsPaid(id)); java.time.LocalDate paidDate = payroll.getById(id).getPaymentDate();
        stale.setBonuses(new BigDecimal("50.25"));
        assertFalse(payroll.updatePayrollEntry(stale)); assertFalse(payroll.deletePayrollEntry(id)); assertFalse(payroll.markAsPaid(id));
        assertEquals(paidDate, payroll.getById(id).getPaymentDate());
        assertEquals(new BigDecimal("1234.56"), payroll.getById(id).getNetSalary());
    }
    @Test void decimalAdjustmentsAreExactAndGenerationDoesNotOverwriteThem() throws Exception {
        payroll.createPayrollEntry(entry(1)); int id = count("SELECT id FROM payroll_entries");
        PayrollEntry value = payroll.getById(id); value.setBonuses(new BigDecimal("0.10")); value.setDeductions(new BigDecimal("0.20"));
        assertTrue(payroll.updatePayrollEntry(value)); assertEquals(new BigDecimal("1234.46"), payroll.getById(id).getNetSalary());
        assertTrue(payroll.generateBatch(List.of(entry(1))).isEmpty()); assertEquals(new BigDecimal("1234.46"), payroll.getById(id).getNetSalary());
    }
    @Test void migrationReportsDuplicatesWithoutDeletingHistory() throws Exception {
        sql("DROP INDEX uq_payroll_employee_period");
        sql("INSERT INTO payroll_entries(employee_id,month,year,status) VALUES(1,9,2026,'PAID'),(1,9,2026,'PENDING')");
        try (Connection conn = connection()) {
            PayrollMigration.Failure error = assertThrows(PayrollMigration.Failure.class, () -> PayrollMigration.apply(conn));
            assertTrue(error.getMessage().contains("employee=1 period=9/2026 records=2"));
        }
        assertEquals(2, count("SELECT COUNT(*) FROM payroll_entries"));
    }
    @Test void migrationIsIdempotentAndRejectsNewDuplicatePeriods() throws Exception {
        try (Connection conn = connection()) { PayrollMigration.apply(conn); }
        payroll.createPayrollEntry(entry(1));
        assertThrows(SQLException.class, () -> sql("INSERT INTO payroll_entries(employee_id,month,year) VALUES(1,9,2026)"));
    }
    @Test void roleAssignmentSynchronizesBothColumnsAndProtectsLastAdmin() throws Exception {
        assertThrows(ManagementException.class, () -> access.assignRole(1, 1, 2));
        assertThrows(ManagementException.class, () -> access.deleteUser(1, 1));
        access.assignRole(1, 3, 2);
        assertEquals(1, count("SELECT COUNT(*) FROM users WHERE id=3 AND role_id=2 AND role='HR'"));
        assertEquals(1, count("SELECT COUNT(*) FROM users WHERE role='ADMIN'"));
    }
    @Test void nonAdminCannotEscalateOrDeleteAdministrator() {
        assertEquals(403, assertThrows(ManagementException.class, () -> access.assignRole(2, 2, 1)).getStatus());
        assertEquals(403, assertThrows(ManagementException.class, () -> access.assignRole(3, 3, 2)).getStatus());
        assertEquals(403, assertThrows(ManagementException.class, () -> access.deleteUser(2, 1)).getStatus());
    }
    @Test void assignedAndSystemRolesCannotBeDeleted() {
        assertEquals(409, assertThrows(ManagementException.class, () -> access.deleteRole(2)).getStatus());
        assertEquals(409, assertThrows(ManagementException.class, () -> access.deleteRole(4)).getStatus());
    }
    @Test void referencedDepartmentsArePreservedEvenWhenForeignKeyWouldSetNull() throws Exception {
        sql("CREATE TABLE departments(id INT PRIMARY KEY, name VARCHAR(100), code VARCHAR(10))");
        sql("CREATE TABLE courses(id INT PRIMARY KEY, department_id INT REFERENCES departments(id) ON DELETE SET NULL)");
        sql("INSERT INTO departments VALUES(1,'Computer Science','CSE')");
        sql("INSERT INTO courses VALUES(1,1)");
        DepartmentDAO departments = new DepartmentDAO(this::connection);
        assertEquals(409, assertThrows(ManagementException.class, () -> departments.deleteDepartment(1)).getStatus());
        assertEquals(1, count("SELECT department_id FROM courses WHERE id=1"));
        sql("DELETE FROM courses");
        assertTrue(departments.deleteDepartment(1));
    }
    @Test void employeeDirectoryIncludesStandaloneStaffButNotLinkedStudents() throws Exception {
        sql("ALTER TABLE roles ADD name VARCHAR(100)"); sql("UPDATE roles SET name=code");
        sql("ALTER TABLE users ADD username VARCHAR(50)"); sql("UPDATE users SET username='staff' || id");
        sql("INSERT INTO roles VALUES(5,'STUDENT',TRUE,'Student')");
        sql("INSERT INTO users VALUES(4,5,'STUDENT','student4')");
        sql("CREATE TABLE faculty(user_id INT, name VARCHAR(100), email VARCHAR(100))");
        for (String definition : List.of("employee_id VARCHAR(50)", "first_name VARCHAR(100)", "last_name VARCHAR(100)", "email VARCHAR(100)", "phone VARCHAR(20)", "designation VARCHAR(100)", "join_date DATE", "salary DECIMAL(10,2)", "status VARCHAR(20)")) sql("ALTER TABLE employees ADD " + definition);
        sql("UPDATE employees SET employee_id=CASE id WHEN 1 THEN 'staff1' ELSE 'CONTRACTOR' END, first_name='Staff', designation='Technician', salary=1000, status='ACTIVE'");
        sql("INSERT INTO employees(id,employee_id,first_name,status) VALUES(3,'student4','Student','ACTIVE')");
        EmployeeDAO employees = new EmployeeDAO(this::connection);
        var list = employees.getAllEmployees();
        assertEquals(4, list.size());
        assertTrue(list.stream().anyMatch(e -> "CONTRACTOR".equals(e.getEmployeeId())));
        assertFalse(list.stream().anyMatch(e -> "student4".equals(e.getEmployeeId())));
        com.college.models.Employee studentProfile = new com.college.models.Employee();
        studentProfile.setEmployeeId("student4");
        assertEquals(409, assertThrows(ManagementException.class, () -> employees.addEmployee(studentProfile)).getStatus());
        var linked = list.stream().filter(e -> e.getId() == 1).findFirst().orElseThrow();
        assertEquals(1, linked.getUserId()); assertEquals("ADMIN", linked.getDesignation());
        var standalone = list.stream().filter(e -> e.getId() == 2).findFirst().orElseThrow();
        standalone.setJoinDate(null); assertTrue(employees.updateEmployee(standalone));
        assertEquals(1, count("SELECT COUNT(*) FROM employees WHERE id=2 AND join_date IS NULL"));
    }
}
