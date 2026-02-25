package com.college.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.college.dao.CommunityDAO;
import com.college.models.Campaign;
import com.college.utils.JsonHelper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CrowdfundingController extends BaseController implements HttpHandler {

    private final CommunityDAO communityDAO = new CommunityDAO();

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (handleOptions(t))
            return;

        String method = t.getRequestMethod();
        String path = t.getRequestURI().getPath();

        try {
            if (path.matches(".*/campaigns")) {
                if ("GET".equals(method))
                    handleGetCampaigns(t);
                else if ("POST".equals(method))
                    handleCreateCampaign(t);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else if (path.matches(".*/campaigns/\\d+/donate")) {
                if ("POST".equals(method))
                    handleDonate(t, path);
                else
                    sendResponse(t, 405, errorJson("Method not allowed"));
            } else {
                sendResponse(t, 404, errorJson("Not found"));
            }
        } catch (Exception e) {
            sendResponse(t, 500, errorJson(e.getMessage() != null ? e.getMessage() : "Internal server error"));
        }
    }

    private void handleGetCampaigns(HttpExchange t) throws IOException {
        List<Campaign> list = communityDAO.getAllCampaigns();
        sendResponse(t, 200, JsonHelper.toJson(list));
    }

    @SuppressWarnings("unchecked")
    private void handleCreateCampaign(HttpExchange t) throws IOException {
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        Campaign c = new Campaign();
        c.setTitle((String) map.get("title"));
        c.setDescription((String) map.get("description"));
        c.setGoalAmount(((Double) map.get("goalAmount")));
        c.setCreatedBy(((Double) map.get("createdBy")).intValue());
        c.setStatus((String) map.get("status"));

        boolean ok = communityDAO.createCampaign(c);
        if (ok)
            sendResponse(t, 201, "{\"message\":\"Campaign created successfully\"}");
        else
            sendResponse(t, 400, errorJson("Failed to create campaign"));
    }

    @SuppressWarnings("unchecked")
    private void handleDonate(HttpExchange t, String path) throws IOException {
        String[] parts = path.split("/");
        int campaignId = Integer.parseInt(parts[parts.length - 2]); // /campaigns/{id}/donate
        String body = readBody(t);
        Map<String, Object> map = new com.google.gson.Gson().fromJson(body, Map.class);

        double amount = ((Double) map.get("amount"));

        boolean ok = communityDAO.donateToCampaign(campaignId, amount);
        if (ok)
            sendResponse(t, 200, "{\"message\":\"Donation successful\"}");
        else
            sendResponse(t, 400, errorJson("Failed to process donation"));
    }
}
