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

    public AllocateRoomDialog(Frame parent, int userId) {
        super(parent, "Allocate Room", true);
        this.userId = userId;
        this.hostelDAO = new HostelDAO();
        this.studentDAO = new StudentDAO();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(500, 350);
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
        studentCombo = new JComboBox<>();
        studentCombo.setPreferredSize(new Dimension(300, 30));
        formPanel.add(studentCombo, gbc);

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

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);

        JButton allocateButton = UIHelper.createSuccessButton("Allocate Room");
        allocateButton.setPreferredSize(new Dimension(140, 35));
        allocateButton.addActionListener(e -> allocateRoom());

        JButton cancelButton = UIHelper.createDangerButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 35));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(allocateButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Load students
        List<Student> students = studentDAO.getAllStudents();
        for (Student student : students) {
            studentCombo.addItem(new StudentItem(student));
        }

        // Load available rooms from all hostels
        List<com.college.models.Hostel> hostels = hostelDAO.getAllHostels();
        for (com.college.models.Hostel hostel : hostels) {
            List<Room> rooms = hostelDAO.getAvailableRooms(hostel.getId());
            for (Room room : rooms) {
                roomCombo.addItem(new RoomItem(room));
            }
        }
    }

    private void allocateRoom() {
        StudentItem selectedStudent = (StudentItem) studentCombo.getSelectedItem();
        RoomItem selectedRoom = (RoomItem) roomCombo.getSelectedItem();

        if (selectedStudent == null || selectedRoom == null) {
            UIHelper.showErrorMessage(this, "Please select both student and room!");
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
            UIHelper.showErrorMessage(this, "Failed to allocate room!");
        }
    }

    private static class StudentItem {
        Student student;

        StudentItem(Student s) {
            this.student = s;
        }

        @Override
        public String toString() {
            return student.getName() + " (" + student.getEmail() + ")";
        }
    }

    private static class RoomItem {
        Room room;

        RoomItem(Room r) {
            this.room = r;
        }

        @Override
        public String toString() {
            return room.getHostelName() + " - Room " + room.getRoomNumber() +
                    " (Floor " + room.getFloor() + ", " + room.getRoomType() +
                    ", Available: " + (room.getCapacity() - room.getOccupiedCount()) + ")";
        }
    }
}
