package com.college.utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for exporting JTable data to various formats
 */
public class TableExporter {

    /**
     * Export table data to CSV file
     * 
     * @param table JTable to export
     * @param file  Output file
     * @return true if successful, false otherwise
     */
    public static boolean exportToCSV(JTable table, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            TableModel model = table.getModel();

            // Write headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.write(escapeCSV(model.getColumnName(col)));
                if (col < model.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");

            // Write data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    writer.write(escapeCSV(value != null ? value.toString() : ""));
                    if (col < model.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }

            return true;
        } catch (IOException e) {
            Logger.error("Error exporting to CSV", e);
            return false;
        }
    }

    /**
     * Export table data to Excel-compatible format (CSV with .xls extension)
     * This creates a tab-delimited file that Excel can open
     * 
     * @param table JTable to export
     * @param file  Output file
     * @return true if successful, false otherwise
     */
    public static boolean exportToExcel(JTable table, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            TableModel model = table.getModel();

            // Write headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.write(model.getColumnName(col));
                if (col < model.getColumnCount() - 1) {
                    writer.write("\t");
                }
            }
            writer.write("\n");

            // Write data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    writer.write(value != null ? value.toString() : "");
                    if (col < model.getColumnCount() - 1) {
                        writer.write("\t");
                    }
                }
                writer.write("\n");
            }

            return true;
        } catch (IOException e) {
            Logger.error("Error exporting to Excel", e);
            return false;
        }
    }

    /**
     * Show export dialog and export table
     * 
     * @param parent          Parent component
     * @param table           Table to export
     * @param defaultFileName Default file name
     */
    public static void showExportDialog(JComponent parent, JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Table");
        fileChooser.setSelectedFile(new File(defaultFileName + ".csv"));

        // Add file filters
        javax.swing.filechooser.FileNameExtensionFilter csvFilter = new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files (*.csv)", "csv");
        javax.swing.filechooser.FileNameExtensionFilter xlsFilter = new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel Files (*.xls)", "xls");

        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(xlsFilter);
        fileChooser.setFileFilter(csvFilter);

        int result = fileChooser.showSaveDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getName().toLowerCase();

            boolean success;
            if (fileName.endsWith(".xls")) {
                success = exportToExcel(table, file);
            } else {
                // Default to CSV
                if (!fileName.endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }
                success = exportToCSV(table, file);
            }

            if (success) {
                JOptionPane.showMessageDialog(parent,
                        "Table exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parent,
                        "Failed to export table!",
                        "Export Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Escape CSV special characters
     */
    private static String escapeCSV(String value) {
        if (value == null)
            return "";

        // If value contains comma, quote, or newline, wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            // Escape quotes by doubling them
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
