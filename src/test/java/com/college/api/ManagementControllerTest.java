package com.college.api;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;
import com.college.utils.ManagementException;
import com.college.utils.ManagementValidation;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ManagementControllerTest {
    private HttpExchange exchange(String method, String path, String body) {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestURI()).thenReturn(URI.create(path));
        when(exchange.getRequestHeaders()).thenReturn(new Headers());
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return exchange;
    }
    private PayrollController payroll(PayrollDAO dao, EmployeeDAO employees) {
        return new PayrollController(dao, employees) {
            @Override protected boolean requirePermission(HttpExchange exchange, String permission) { return true; }
        };
    }
    @Test void validatesPayrollPeriodsWithoutTruncatingFractions() {
        for (String input : List.of("{\"month\":0}", "{\"month\":13}", "{\"month\":1.5}", "{\"year\":0}", "{\"year\":10000}", "{\"month\":null}"))
            assertThrows(ManagementException.class, () -> PayrollController.period(ManagementValidation.object(input)));
        assertArrayEquals(new int[]{2, 2026}, PayrollController.period(ManagementValidation.object("{\"month\":2,\"year\":2026}")));
    }
    @Test void rejectsInvalidMoneyAndPermissionIdentifiers() {
        for (String value : List.of("-1", "0.001", "null", "\"NaN\"", "100000000"))
            assertThrows(ManagementException.class, () -> ManagementValidation.money(ManagementValidation.object("{\"salary\":" + value + "}"), "salary", BigDecimal.ZERO));
        for (String value : List.of("[1.5]", "[0]", "[-1]", "[null]", "[true]", "[2147483648]"))
            assertThrows(IllegalArgumentException.class, () -> RoleController.parsePermissionIds(value));
        assertEquals(List.of(1, 2), RoleController.parsePermissionIds("[1,\"2\",1]"));
    }
    @Test void employeeValidationAcceptsRealEmailAndPreservesMissingDate() {
        Employee employee = EmployeeController.parseEmployee(ManagementValidation.object("{\"employeeId\":\" EMP1 \",\"firstName\":\" Alice \",\"email\":\"alice@example.org\",\"designation\":\"Teacher\",\"salary\":\"25000.50\",\"joinDate\":\"\"}"));
        assertEquals("EMP1", employee.getEmployeeId()); assertEquals("Alice", employee.getFirstName());
        assertEquals(new BigDecimal("25000.50"), employee.getSalary()); assertNull(employee.getJoinDate());
        assertThrows(ManagementException.class, () -> EmployeeController.parseEmployee(ManagementValidation.object("{}")));
    }
    @Test void profileSetupCannotUseUpdatePermission() throws Exception {
        EmployeeDAO employees = mock(EmployeeDAO.class);
        EmployeeController controller = new EmployeeController(employees) {
            @Override protected boolean requirePermission(HttpExchange exchange, String permission) { return true; }
        };
        HttpExchange exchange = exchange("PUT", "/api/employees", "{\"id\":0,\"employeeId\":\"EMP1\",\"firstName\":\"Alice\",\"email\":\"alice@example.org\",\"designation\":\"Teacher\",\"salary\":0}");
        controller.handle(exchange);
        verify(exchange).sendResponseHeaders(eq(400), anyLong()); verifyNoInteractions(employees);
    }
    @Test void eligibilityExplainsMissingProfilesSalaryAndFutureJoinDates() {
        Employee employee = new Employee(); employee.setStatus(Employee.Status.ACTIVE);
        assertEquals("Employee profile not saved", PayrollController.eligibility(employee, 2, 2026));
        employee.setId(1); employee.setSalary(BigDecimal.ZERO);
        assertEquals("Set a positive monthly salary", PayrollController.eligibility(employee, 2, 2026));
        employee.setSalary(new BigDecimal("100")); employee.setJoinDate(LocalDate.of(2026, 3, 1));
        assertEquals("Joining date is after this period", PayrollController.eligibility(employee, 2, 2026));
        employee.setJoinDate(LocalDate.of(2026, 2, 28)); assertNull(PayrollController.eligibility(employee, 2, 2026));
        employee.setStatus(Employee.Status.ON_LEAVE); assertEquals("Not active", PayrollController.eligibility(employee, 2, 2026));
    }
    @Test void paidEntriesCannotBeChangedAndPaymentIsIdempotent() throws Exception {
        PayrollDAO dao = mock(PayrollDAO.class); EmployeeDAO employees = mock(EmployeeDAO.class);
        PayrollEntry paid = new PayrollEntry(1, 2, 2026, BigDecimal.TEN); paid.setStatus(PayrollEntry.Status.PAID);
        when(dao.getById(1)).thenReturn(paid);
        for (String method : List.of("PUT", "DELETE")) {
            HttpExchange request = exchange(method, "/api/payroll/1", "{\"bonuses\":10}"); payroll(dao, employees).handle(request);
            verify(request).sendResponseHeaders(eq(409), anyLong());
        }
        HttpExchange payment = exchange("POST", "/api/payroll/mark-paid", "{\"id\":1}"); payroll(dao, employees).handle(payment);
        verify(payment).sendResponseHeaders(eq(200), anyLong());
        verify(dao, never()).markAsPaid(anyInt()); verify(dao, never()).updatePayrollEntry(any()); verify(dao, never()).deletePayrollEntry(anyInt());
    }
    @Test void unauthenticatedRequestsCannotReadEmployeeOrPayrollData() throws Exception {
        EmployeeDAO employees = mock(EmployeeDAO.class); PayrollDAO dao = mock(PayrollDAO.class);
        HttpExchange request = exchange("GET", "/api/employees", ""); new EmployeeController(employees).handle(request);
        verify(request).sendResponseHeaders(eq(401), anyLong());
        request = exchange("GET", "/api/payroll", ""); new PayrollController(dao, employees).handle(request);
        verify(request).sendResponseHeaders(eq(401), anyLong()); verifyNoInteractions(employees, dao);
    }
}
