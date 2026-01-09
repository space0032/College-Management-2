package com.college.dao;

import com.college.models.*;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDetailsDAO {

    // --- Collaborators ---
    public boolean addCollaborator(int eventId, int deptId) {
        String sql = "INSERT INTO event_collaborators (event_id, department_id, status) VALUES (?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, deptId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error adding collaborator: " + e.getMessage());
            return false;
        }
    }

    public List<EventCollaborator> getCollaborators(int eventId) {
        List<EventCollaborator> list = new ArrayList<>();
        String sql = "SELECT ec.*, d.name as dept_name " +
                "FROM event_collaborators ec " +
                "JOIN departments d ON ec.department_id = d.id " +
                "WHERE ec.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventCollaborator ec = new EventCollaborator();
                ec.setId(rs.getInt("id"));
                ec.setEventId(rs.getInt("event_id"));
                ec.setDepartmentId(rs.getInt("department_id"));
                ec.setStatus(rs.getString("status"));
                ec.setDepartmentName(rs.getString("dept_name"));
                list.add(ec);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching collaborators: " + e.getMessage());
        }
        return list;
    }

    // --- Resources ---
    public boolean addResource(EventResource res) {
        String sql = "INSERT INTO event_resources (event_id, resource_name, quantity, status) VALUES (?, ?, ?, 'REQUESTED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, res.getEventId());
            pstmt.setString(2, res.getResourceName());
            pstmt.setInt(3, res.getQuantity());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error adding resource: " + e.getMessage());
            return false;
        }
    }

    public List<EventResource> getResources(int eventId) {
        List<EventResource> list = new ArrayList<>();
        String sql = "SELECT * FROM event_resources WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventResource er = new EventResource();
                er.setId(rs.getInt("id"));
                er.setEventId(rs.getInt("event_id"));
                er.setResourceName(rs.getString("resource_name"));
                er.setQuantity(rs.getInt("quantity"));
                er.setStatus(rs.getString("status"));
                list.add(er);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching resources: " + e.getMessage());
        }
        return list;
    }

    // --- Volunteers ---
    public boolean registerVolunteer(int eventId, int studentId, String task) {
        String sql = "INSERT INTO event_volunteers (event_id, student_id, task_description, status) VALUES (?, ?, ?, 'REGISTERED')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, task);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error registering volunteer: " + e.getMessage());
            return false;
        }
    }

    public List<EventVolunteer> getVolunteers(int eventId) {
        List<EventVolunteer> list = new ArrayList<>();
        String sql = "SELECT ev.*, s.name as student_name, e.name as event_name " +
                "FROM event_volunteers ev " +
                "JOIN students s ON ev.student_id = s.id " +
                "JOIN events e ON ev.event_id = e.id " +
                "WHERE ev.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventVolunteer ev = new EventVolunteer();
                ev.setId(rs.getInt("id"));
                ev.setEventId(rs.getInt("event_id"));
                ev.setStudentId(rs.getInt("student_id"));
                ev.setTaskDescription(rs.getString("task_description"));
                ev.setStatus(rs.getString("status"));
                ev.setHoursLogged(rs.getFloat("hours_logged"));
                ev.setStudentName(rs.getString("student_name"));
                ev.setEventName(rs.getString("event_name"));
                list.add(ev);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching volunteers: " + e.getMessage());
        }
        return list;
    }

    public List<EventVolunteer> getVolunteersByStudent(int studentId) {
        List<EventVolunteer> list = new ArrayList<>();
        String sql = "SELECT ev.*, s.name as student_name, e.name as event_name " +
                "FROM event_volunteers ev " +
                "JOIN students s ON ev.student_id = s.id " +
                "JOIN events e ON ev.event_id = e.id " +
                "WHERE ev.student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventVolunteer ev = new EventVolunteer();
                ev.setId(rs.getInt("id"));
                ev.setEventId(rs.getInt("event_id"));
                ev.setStudentId(rs.getInt("student_id"));
                ev.setTaskDescription(rs.getString("task_description"));
                ev.setStatus(rs.getString("status"));
                ev.setHoursLogged(rs.getFloat("hours_logged"));
                ev.setStudentName(rs.getString("student_name"));
                ev.setEventName(rs.getString("event_name"));
                list.add(ev);
            }
        } catch (SQLException e) {
            Logger.error("Error fetching volunteer tasks: " + e.getMessage());
        }
        return list;
    }

    public boolean isVolunteer(int eventId, int studentId) {
        String sql = "SELECT 1 FROM event_volunteers WHERE event_id = ? AND student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            pstmt.setInt(2, studentId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateVolunteerTask(int volunteerId, String task, String status) {
        String sql = "UPDATE event_volunteers SET task_description = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task);
            pstmt.setString(2, status);
            pstmt.setInt(3, volunteerId);

            boolean success = pstmt.executeUpdate() > 0;
            if (success && "APPROVED".equals(status)) {
                // Fetch student ID for notification
                String fetchSql = "SELECT student_id, e.name FROM event_volunteers ev JOIN events e ON ev.event_id = e.id WHERE ev.id = ?";
                try (PreparedStatement fetchStmt = conn.prepareStatement(fetchSql)) {
                    fetchStmt.setInt(1, volunteerId);
                    ResultSet rs = fetchStmt.executeQuery();
                    if (rs.next()) {
                        int studentId = rs.getInt("student_id");
                        String eventName = rs.getString("name");

                        String userSql = "SELECT user_id FROM students WHERE id = ?";
                        try (PreparedStatement uStmt = conn.prepareStatement(userSql)) {
                            uStmt.setInt(1, studentId);
                            ResultSet uRs = uStmt.executeQuery();
                            if (uRs.next()) {
                                int userId = uRs.getInt("user_id");
                                Notification note = new Notification();
                                note.setRecipientUserId(userId);
                                note.setRecipientContact("");
                                note.setType(Notification.Type.SYSTEM);
                                note.setSubject("New Volunteer Task Assigned");
                                note.setMessage("You have been assigned a task for event '" + eventName + "': " + task);
                                note.setStatus(Notification.Status.PENDING);
                                note.setCreatedAt(java.time.LocalDateTime.now());

                                new NotificationDAO().createNotification(note);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    Logger.error("Error sending notification: " + ex.getMessage());
                }
            }
            return success;
        } catch (SQLException e) {
            Logger.error("Error updating volunteer task: " + e.getMessage());
            return false;
        }
    }

    public boolean updateVolunteerHours(int volunteerId, float hours, String status) {
        String sql = "UPDATE event_volunteers SET hours_logged = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setFloat(1, hours);
            pstmt.setString(2, status);
            pstmt.setInt(3, volunteerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating volunteer hours: " + e.getMessage());
            return false;
        }
    }

    public boolean updateResourceStatus(int resourceId, String status) {
        String sql = "UPDATE event_resources SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, resourceId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating resource status: " + e.getMessage());
            return false;
        }
    }

    // --- Budgets ---
    public boolean addBudget(EventBudget budget) {
        String sql = "INSERT INTO event_budgets (event_id, item, estimated_cost, actual_cost, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, budget.getEventId());
            pstmt.setString(2, budget.getItem());
            pstmt.setDouble(3, budget.getEstimatedCost());
            pstmt.setDouble(4, budget.getActualCost());
            pstmt.setString(5, budget.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // If table doesn't exist, create it on fly (Development convenience)
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createBudgetTable();
                return addBudget(budget);
            }
            Logger.error("Error adding budget: " + e.getMessage());
            return false;
        }
    }

    private void createBudgetTable() {
        String sql = "CREATE TABLE IF NOT EXISTS event_budgets (" +
                "id SERIAL PRIMARY KEY, " +
                "event_id INT, " +
                "item VARCHAR(255), " +
                "estimated_cost DOUBLE, " +
                "actual_cost DOUBLE, " +
                "status VARCHAR(50), " +
                "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE)";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.error("Error creating budget table: " + e.getMessage());
        }
    }

    public List<EventBudget> getBudgets(int eventId) {
        List<EventBudget> list = new ArrayList<>();
        String sql = "SELECT * FROM event_budgets WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventBudget b = new EventBudget();
                b.setId(rs.getInt("id"));
                b.setEventId(rs.getInt("event_id"));
                b.setItem(rs.getString("item"));
                b.setEstimatedCost(rs.getDouble("estimated_cost"));
                b.setActualCost(rs.getDouble("actual_cost"));
                b.setStatus(rs.getString("status"));
                list.add(b);
            }
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createBudgetTable(); // Ensure table exists
                return list;
            }
            Logger.error("Error fetching budgets: " + e.getMessage());
        }
        return list;
    }

    public boolean updateBudgetStatus(int id, String status) {
        String sql = "UPDATE event_budgets SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating budget: " + e.getMessage());
            return false;
        }
    }

    // --- Polls ---
    public boolean createPoll(EventPoll poll) {
        String sql = "INSERT INTO event_polls (event_id, question, options, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, poll.getEventId());
            pstmt.setString(2, poll.getQuestion());
            pstmt.setString(3, poll.getOptions());
            pstmt.setString(4, poll.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createPollTable();
                return createPoll(poll);
            }
            Logger.error("Error creating poll: " + e.getMessage());
            return false;
        }
    }

    private void createPollTable() {
        String sql = "CREATE TABLE IF NOT EXISTS event_polls (" +
                "id SERIAL PRIMARY KEY, " +
                "event_id INT, " +
                "question VARCHAR(255), " +
                "options TEXT, " +
                "status VARCHAR(50), " +
                "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE)";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            Logger.error("Error creating poll table: " + e.getMessage());
        }
    }

    public List<EventPoll> getPolls(int eventId) {
        List<EventPoll> list = new ArrayList<>();
        String sql = "SELECT * FROM event_polls WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, eventId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                EventPoll p = new EventPoll();
                p.setId(rs.getInt("id"));
                p.setEventId(rs.getInt("event_id"));
                p.setQuestion(rs.getString("question"));
                p.setOptions(rs.getString("options"));
                p.setStatus(rs.getString("status"));
                list.add(p);
            }
        } catch (SQLException e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("not exist") || msg.contains("doesn't exist")) {
                createPollTable();
                return list;
            }
            Logger.error("Error fetching polls: " + e.getMessage());
        }
        return list;
    }

}
