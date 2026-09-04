package com.college.services;

/**
 * Successful chat result from any AI provider.
 * {@code model} carries a provider prefix (e.g. "tokenra/..." or
 * "openrouter/...") so audit logs and the UI always show what served
 * the request.
 */
public final class ChatResult {
    public final String model;
    public final String text;

    public ChatResult(String model, String text) {
        this.model = model;
        this.text = text;
    }
}
