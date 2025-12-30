package com.college.ui.hostel;

import com.college.dao.HostelDAO;
import com.college.dao.StudentDAO;
import com.college.models.HostelAllocation;
import com.college.models.Room;
import com.college.models.Student;
import com.college.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Allocate Room Dialog
 */
public class AllocateRoomDialog extends JDialog {

    private HostelDAO hostelDAO;
    private StudentDAO studentDAO;
    private int userId;

    private JComboBox<StudentItem> studentCombo;
    private JComboBox<RoomItem> roomCombo;
    private JTextArea remarksArea;

    private JLabel statusLabel;

    public AllocateRoomDialog(Frame parent, int userId) {
        super(parent, "Allocate Room", true);
        this.userId = userId;
        this.hostelDAO = new HostelDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(550, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Student selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(UIHelper.createLabel("Select Student:"), gbc);

        gbc.gridx = 1;
        JPanel studentPanel = new JPanel(new BorderLayout(5, 0));
        studentPanel.setBackground(Color.WHITE);

        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(220, 30));

        JButton searchButton = new JButton("Search");
        searchButton.setToolTipText("Search by name");
        searchButton.addActionListener(e -> {
            String keyword = JOptionPane.showInputDialog(this, "Enter student name/email:", "Search Student",
                    JOptionPane.QUESTION_MESSAGE);
            if (keyword != null && !keyword.trim().isEmpty()) {
                filterStudents(keyword.trim());
            }
        });

        studentPanel.add(studentCombo, BorderLayout.CENTER);
        studentPanel.add(searchButton, BorderLayout.EAST);

        formPanel.add(studentPanel, gbc);

        // Room selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(UIHelper.createLabel("Select Room:"), gbc);

        gbc.gridx = 1;
        roomCombo = new JComboBox<>();
        roomCombo.setPreferredSize(new Dimension(300, 30));
        formPanel.add(roomCombo, gbc);

        // Remarks
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(UIHelper.createLabel("Remarks:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        remarksArea = new JTextArea(5, 20);
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        JScrollPane scrollPane = new JScrollPane(remarksArea);
        formPanel.add(scrollPane, gbc);

        // Status Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(UIHelper.SUCCESS_COLOR);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        formPanel.add(statusLabel, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton allocateButton = UIHelper.createSuccessButton("Allocate Room");
        allocateButton.setPreferredSize(new Dimension(140, 35));
        allocateButton.addActionListener(e -> allocateRoom());

        JButton closeButton = UIHelper.createDangerButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 35));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(allocateButton);
        buttonPanel.add(closeButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Load all students initially
        reloadStudents(studentDAO.getAllStudents());
        reloadRooms();
    }

    private void reloadRooms() {
        RoomItem selected = (RoomItem) roomCombo.getSelectedItem();
        int selectedId = (selected != null) ? selected.room.getId() : -1;

        roomCombo.removeAllItems();
        // Load available rooms from all hostels
        List<com.college.models.Hostel> hostels = hostelDAO.getAllHostels();
        for (com.college.models.Hostel hostel : hostels) {
            List<Room> rooms = hostelDAO.getAvailableRooms(hostel.getId());
            for (Room room : rooms) {
                RoomItem item = new RoomItem(room);
                roomCombo.addItem(item);
                if (room.getId() == selectedId) {
                    roomCombo.setSelectedItem(item);
                }
            }
        }
    }

    private void filterStudents(String keyword) {
        List<Student> students = studentDAO.searchStudents(keyword);
        reloadStudents(students);
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students found matching '" + keyword + "'");
        }
    }

    private void reloadStudents(List<Student> students) {
        studentCombo.removeAllItems();
        for (Student student : students) {
            studentCombo.addItem(new StudentItem(student));
        }
    }

    private void allocateRoom() {
        StudentItem selectedStudent = (StudentItem) studentCombo.getSelectedItem();
        RoomItem selectedRoom = (RoomItem) roomCombo.getSelectedItem();

        if (selectedStudent == null || selectedRoom == null) {
            statusLabel.setText("Please select both student and room!");
            statusLabel.setForeground(UIHelper.DANGER_COLOR);
            return;
        }

        HostelAllocation allocation = new HostelAllocation(
                selectedStudent.student.getId(),
                selectedRoom.room.getId());
        allocation.setAllocatedBy(userId);
        allocation.setRemarks(remarksArea.getText().trim());

        if (hostelDAO.allocateRoom(allocation)) {
            UIHelper.showSuccessMessage(this,
                    "Room allocated successfully!\n\n" +
                            "Student: " + selectedStudent.student.getName() + "\n" +
                            "Room: " + selectedRoom.room.getRoomNumber() + " - " + selectedRoom.room.getHostelName());
            dispose();
        } else {
            statusLabel.setText("Failed to allocate room!");
            statusLabel.setForeground(UIHelper.DANGER_COLOR);
        }
    }

    private static class StudentItem {
        Student student;

        StudentItem(Student s) {
            this.student = s;
        }

        @Override
        public String toString() {
            String idDisplay = student.getUsername() != null ? student.getUsername() : String.valueOf(student.getId());
            return student.getName() + " (" + idDisplay + ")";
        }
    }

    private static class RoomItem {
        Room room;

        RoomItem(Room r) {
            this.room = r;
        }

        // Needed for selection logic? ComboBox loops equals?
        // Default equals is reference, so we need to rely on ID check in loop

        @Override
        public String toString() {
            return room.getHostelName() + " - Room " + room.getRoomNumber() +
                    " (Floor " + room.getFloor() + ", " + room.getRoomType() +
                    ", Available: " + (room.getCapacity() - room.getOccupiedCount()) + ")";
        }
    }
}
