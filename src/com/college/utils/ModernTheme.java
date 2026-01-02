package com.college.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Modern Theme for College Management System
 * Provides a cohesive dark/light modern color palette and styling
 */
public class ModernTheme {

    // === MODERN COLOR PALETTE ===
    
    // Primary Colors (Teal/Emerald theme - More professional)
    public static final Color PRIMARY = new Color(20, 184, 166);       // Teal-500
    public static final Color PRIMARY_DARK = new Color(13, 148, 136);  // Teal-600
    public static final Color PRIMARY_LIGHT = new Color(94, 234, 212); // Teal-300
    
    // Sidebar Colors (Dark slate theme - Clean and modern)
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Slate-900
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate-800
    public static final Color SIDEBAR_TEXT = new Color(203, 213, 225); // Slate-300
    public static final Color SIDEBAR_ACTIVE = new Color(20, 184, 166); // Teal accent
    
    // Background Colors
    public static final Color BG_MAIN = new Color(248, 250, 252);      // Slate-50
    public static final Color BG_CARD = Color.WHITE;                    // Card background
    public static final Color BG_DARK = new Color(15, 23, 42);         // Slate-900
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // Slate-900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate-500
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);   // Slate-400
    public static final Color TEXT_WHITE = Color.WHITE;
    
    // Status Colors (Vibrant but professional)
    public static final Color SUCCESS = new Color(34, 197, 94);        // Green-500
    public static final Color SUCCESS_LIGHT = new Color(220, 252, 231); // Green-100
    public static final Color DANGER = new Color(239, 68, 68);         // Red-500
    public static final Color DANGER_LIGHT = new Color(254, 226, 226); // Red-100
    public static final Color WARNING = new Color(245, 158, 11);       // Amber-500
    public static final Color WARNING_LIGHT = new Color(254, 243, 199); // Amber-100
    public static final Color INFO = new Color(59, 130, 246);          // Blue-500
    public static final Color INFO_LIGHT = new Color(219, 234, 254);   // Blue-100
    
    // Border Colors
    public static final Color BORDER = new Color(226, 232, 240);       // Slate-200
    public static final Color BORDER_DARK = new Color(203, 213, 225);  // Slate-300
    
    // Chart Colors (Beautiful gradient-friendly)
    public static final Color CHART_1 = new Color(20, 184, 166);   // Teal
    public static final Color CHART_2 = new Color(59, 130, 246);   // Blue
    public static final Color CHART_3 = new Color(168, 85, 247);   // Purple
    public static final Color CHART_4 = new Color(236, 72, 153);   // Pink
    public static final Color CHART_5 = new Color(245, 158, 11);   // Amber
    
    // === FONTS ===
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    
    // === DIMENSIONS ===
    public static final int SIDEBAR_WIDTH = 240;
    public static final int CARD_RADIUS = 12;
    public static final int BUTTON_RADIUS = 8;
    public static final Dimension BUTTON_SIZE = new Dimension(140, 38);
    public static final Dimension BUTTON_SMALL = new Dimension(100, 32);
    
    // === FACTORY METHODS ===
    
    /**
     * Create a modern card panel with shadow effect
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, CARD_RADIUS, CARD_RADIUS);
                
                // Draw card background
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, CARD_RADIUS, CARD_RADIUS);
                
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return card;
    }
    
    /**
     * Create a modern rounded button
     */
    public static JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_RADIUS, BUTTON_RADIUS);
                
                g2.setColor(TEXT_WHITE);
                g2.setFont(FONT_BUTTON);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                
                g2.dispose();
            }
        };
        button.setPreferredSize(BUTTON_SIZE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    /**
     * Create a stat card for dashboard
     */
    public static JPanel createStatCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 10));
        
        // Left accent bar
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 2, 2);
                g2.dispose();
            }
        };
        accentBar.setPreferredSize(new Dimension(4, 0));
        accentBar.setOpaque(false);
        
        // Content panel
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SMALL);
        titleLabel.setForeground(TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_TITLE);
        valueLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(accentColor);
        
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(3));
        content.add(subtitleLabel);
        
        card.add(accentBar, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Create sidebar menu item
     */
    public static JPanel createSidebarItem(String text, boolean isActive) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isActive) {
                    g2.setColor(SIDEBAR_ACTIVE);
                    g2.fillRoundRect(5, 2, getWidth() - 10, getHeight() - 4, 8, 8);
                }
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        item.setOpaque(false);
        item.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(isActive ? TEXT_WHITE : SIDEBAR_TEXT);
        item.add(label, BorderLayout.CENTER);
        
        return item;
    }
    
    /**
     * Apply modern look to entire application
     */
    public static void applyModernLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Button defaults
            UIManager.put("Button.arc", 8);
            UIManager.put("Button.background", PRIMARY);
            UIManager.put("Button.foreground", TEXT_WHITE);
            
            // TextField defaults
            UIManager.put("TextField.arc", 6);
            UIManager.put("TextField.background", Color.WHITE);
            
            // Table defaults
            UIManager.put("Table.alternateRowColor", new Color(249, 250, 251));
            UIManager.put("TableHeader.background", PRIMARY);
            UIManager.put("TableHeader.foreground", TEXT_WHITE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
