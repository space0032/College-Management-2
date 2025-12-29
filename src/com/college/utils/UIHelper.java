package com.college.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Helper class for common UI operations
 * Provides styled components and dialog methods
 */
public class UIHelper {

    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    public static final Color WARNING_COLOR = new Color(243, 156, 18);
    public static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    public static final Color TEXT_COLOR = new Color(44, 62, 80);

    /**
     * Create a styled button
     * 
     * @param text    Button text
     * @param bgColor Background color
     * @return Styled JButton
     */
    public static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    /**
     * Create a primary button (blue)
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }

    /**
     * Create a success button (green)
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR);
    }

    /**
     * Create a danger button (red)
     * 
     * @param text Button text
     * @return Styled JButton
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR);
    }

    /**
     * Create a styled label
     * 
     * @param text Label text
     * @return Styled JLabel
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    /**
     * Create a styled text field
     * 
     * @param columns Number of columns
     * @return Styled JTextField
     */
    public static JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return textField;
    }

    /**
     * Style a table
     * 
     * @param table JTable to style
     */
    public static void styleTable(JTable table) {
        // Header styling
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setReorderingAllowed(false);

        // Table styling
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(189, 195, 199));
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        // Center align cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    /**
     * Show success message dialog
     * 
     * @param parent  Parent component
     * @param message Message to display
     */
    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show error message dialog
     * 
     * @param parent  Parent component
     * @param message Message to display
     */
    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show confirmation dialog
     * 
     * @param parent  Parent component
     * @param message Message to display
     * @return true if confirmed, false otherwise
     */
    public static boolean showConfirmDialog(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message,
                "Confirm", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Create a titled panel
     * 
     * @param title Panel title
     * @return JPanel with titled border
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                title,
                0,
                0,
                new Font("Arial", Font.BOLD, 16),
                PRIMARY_COLOR));
        return panel;
    }
}
