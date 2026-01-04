package com.college.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Enhanced UI Helper with additional styling methods
 */
public class UIHelperExtensions {

    /**
     * Create a modern card panel with shadow effect
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        return card;
    }

    /**
     * Create a section header label
     */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(new Color(52, 73, 94));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return label;
    }

    /**
     * Create a badge label (for counts, status, etc.)
     */
    public static JLabel createBadge(String text, Color bgColor) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Arial", Font.BOLD, 12));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        return badge;
    }

    /**
     * Create an info panel with icon and text
     */
    public static JPanel createInfoPanel(String iconText, String message, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JLabel icon = new JLabel(iconText);
        icon.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(icon, BorderLayout.WEST);
        panel.add(msgLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Add hover effect to component
     */
    public static void addHoverEffect(JComponent component, Color normalColor, Color hoverColor) {
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                component.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                component.setBackground(normalColor);
            }
        });
    }
}
