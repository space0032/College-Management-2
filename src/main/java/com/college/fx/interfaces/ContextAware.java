package com.college.fx.interfaces;

/**
 * Interface for Views/Controllers that can provide context data to the AI
 * ChatBot.
 */
public interface ContextAware {
    /**
     * Returns a text summary of the current data or state visible on the screen.
     * This data is treated as Read-Only by the AI.
     * 
     * @return String context data
     */
    String getContextData();
}
