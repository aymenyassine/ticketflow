package com.ticketflow.eventservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test void draftIsHiddenUntilPublishedAndClosedEventIsHiddenAgain() throws Exception {
        String title = "Concert " + UUID.randomUUID();
        String body = json.writeValueAsString(Map.of("title", title, "description", "Live show",
                "venue", "Rabat", "startsAt", Instant.now().plus(10, ChronoUnit.DAYS).toString(),
                "price", 100, "capacity", 10));
        String created = mvc.perform(post("/api/v1/admin/events")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(created).get("id").asText();
        mvc.perform(get("/api/v1/events/" + id)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/admin/events/" + id + "/publish"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/api/v1/events/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.capacity").value(10));
        mvc.perform(get("/api/v1/events").param("venue", "rabat"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id=='" + id + "')]").exists());
        mvc.perform(post("/api/v1/admin/events/" + id + "/close"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(get("/api/v1/events/" + id)).andExpect(status().isNotFound());
    }
}
