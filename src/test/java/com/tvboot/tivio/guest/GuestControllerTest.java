package com.tvboot.tivio.guest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvboot.tivio.guest.dto.GuestCreateDto;
import com.tvboot.tivio.guest.dto.GuestResponseDto;
import com.tvboot.tivio.guest.dto.GuestUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long createdGuestId;

    @BeforeEach
    void setup() throws Exception {
        // 🔑 Authenticate once
        String loginRequest = """
            {"username":"admin","password":"admin123"}
        """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        token = json.get("token").asText();

        // 👤 Create a guest for tests that need an existing ID
        GuestCreateDto createDto = GuestCreateDto.builder()
                .firstName("Test")
                .lastName("Guest")
                .email("test.guest@email.com")
                .phone("+1111111111")
                .nationality("US")
                .vipStatus(false)
                .build();

        MvcResult guestResult = mockMvc.perform(post("/api/v1/guests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String guestResponse = guestResult.getResponse().getContentAsString();
        JsonNode guestJson = objectMapper.readTree(guestResponse);
        createdGuestId = guestJson.get("data").get("guest").get("id").asLong();
    }

    @Test
    void shouldGetAllGuests() throws Exception {
        mockMvc.perform(get("/api/v1/guests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void shouldGetGuestById() throws Exception {
        mockMvc.perform(get("/api/v1/guests/" + createdGuestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.guest.firstName", is("Test")));
    }

    @Test
    void shouldCreateGuest() throws Exception {
        GuestCreateDto createDto = GuestCreateDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@email.com")
                .phone("+1234567890")
                .nationality("US")
                .vipStatus(false)
                .build();

        mockMvc.perform(post("/api/v1/guests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Guest created successfully"));
    }

    @Test
    void shouldUpdateGuest() throws Exception {
        GuestUpdateDto updateDto = GuestUpdateDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@email.com")
                .build();

        mockMvc.perform(put("/api/v1/guests/" + createdGuestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    void shouldDeleteGuest() throws Exception {
        mockMvc.perform(delete("/api/v1/guests/" + createdGuestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Guest deleted successfully"));
    }
}