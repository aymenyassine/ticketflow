package com.ticketflow.eventservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void draftIsPrivateUntilPublishedAndCapacityIsChecked() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String venue = mvc.perform(post("/api/v1/admin/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Hall " + suffix, "city", "Rabat",
                        "address", "1 Main Street", "capacity", 10))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String venueId = json.readTree(venue).get("id").asText();
        String category = mvc.perform(post("/api/v1/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Music " + suffix))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String categoryId = json.readTree(category).get("id").asText();
        Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
        String event = mvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("title", "Concert " + suffix,
                        "summary", "Live show", "description", "A live concert in Rabat",
                        "startsAt", start.toString(), "endsAt", start.plus(2, ChronoUnit.HOURS).toString(),
                        "organizerId", "organizer-1", "venueId", venueId, "categoryId", categoryId))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String eventId = json.readTree(event).get("id").asText();
        mvc.perform(get("/api/v1/events/" + eventId)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/admin/events/" + eventId + "/publish"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/events/" + eventId + "/tiers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Regular", "price", 100, "capacity", 11))))
                .andExpect(status().isConflict());
        String tier = mvc.perform(post("/api/v1/admin/events/" + eventId + "/tiers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("name", "Regular", "price", 100, "capacity", 10))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String tierId = json.readTree(tier).get("id").asText();
        mvc.perform(post("/api/v1/admin/events/" + eventId + "/publish"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tiers[0].available").value(10));
        mvc.perform(get("/api/v1/events").param("city", "Rabat"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.id=='" + eventId + "')]").exists());
        mvc.perform(get("/api/v1/events").param("minPrice", "90").param("maxPrice", "110"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.id=='" + eventId + "')]").exists());

        String holdId = UUID.randomUUID().toString();
        String holdPath = "/internal/v1/inventory/holds/" + holdId;
        String holdRequest = json.writeValueAsString(Map.of("tierId", tierId, "quantity", 4,
                "expiresAt", Instant.now().plus(1, ChronoUnit.HOURS).toString()));
        mvc.perform(put(holdPath).contentType(MediaType.APPLICATION_JSON).content(holdRequest))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HELD"));
        mvc.perform(put(holdPath).contentType(MediaType.APPLICATION_JSON).content(holdRequest))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(jsonPath("$.tiers[0].available").value(6));
        mvc.perform(delete(holdPath)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));
        mvc.perform(delete(holdPath)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(jsonPath("$.tiers[0].available").value(10));
        mvc.perform(post("/api/v1/admin/events/" + eventId + "/close"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(get("/api/v1/events/" + eventId)).andExpect(status().isNotFound());
    }
}
