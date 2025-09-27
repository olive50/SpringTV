package com.tvboot.tivio.guest;

import com.fasterxml.jackson.databind.JsonNode;
import com.tvboot.tivio.TivioApplication;
import com.tvboot.tivio.base.BaseIntegrationTest;
import com.tvboot.tivio.config.TestConfig;
import com.tvboot.tivio.guest.dto.GuestCreateDto;
import com.tvboot.tivio.guest.dto.GuestUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour GuestController
 * Hérite de BaseIntegrationTest pour la configuration commune
 */
@SpringBootTest(classes = {TivioApplication.class, TestConfig.class})
@ActiveProfiles("test")
class GuestControllerTest extends BaseIntegrationTest {

    @Autowired
    private GuestRepository guestRepository;

    private String token;
    private Long createdGuestId;

    @BeforeEach
    void setup() throws Exception {
        // 🧹 Nettoyer la base de données
        guestRepository.deleteAll();

        // 🔑 Authenticate
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

        // 👤 Create test guest
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
    void shouldCreateGuestSuccessfully() throws Exception {
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
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Guest created successfully"))
                .andExpect(jsonPath("$.data.guest.firstName", is("John")))
                .andExpect(jsonPath("$.data.guest.lastName", is("Doe")));
    }


    @Test
    void shouldUpdateGuest() throws Exception {
        GuestUpdateDto updateDto = GuestUpdateDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@email.com")
                .phone("+1234567890")
                .nationality("FR")
                .build();

        mockMvc.perform(put("/api/v1/guests/" + createdGuestId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Guest updated successfully"))
                .andExpect(jsonPath("$.data.guest.firstName", is("Jane")));
    }

    @Test
    void shouldDeleteGuest() throws Exception {
        mockMvc.perform(delete("/api/v1/guests/" + createdGuestId)
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Guest deleted successfully"))
                .andExpect(jsonPath("$.data.deletedGuestId").value(createdGuestId));
    }

    @Test
    void shouldGetVipGuests() throws Exception {
        // Créer un guest VIP d'abord
        GuestCreateDto vipDto = GuestCreateDto.builder()
                .firstName("VIP")
                .lastName("Guest")
                .email("vip@email.com")
                .phone("+9999999999")
                .nationality("UK")
                .vipStatus(true)
                .build();

        mockMvc.perform(post("/api/v1/guests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vipDto)))
                .andExpect(status().isCreated());

        // Tester l'endpoint VIP
        mockMvc.perform(get("/api/v1/guests/vip")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.guests").isArray())
                .andExpect(jsonPath("$.data.guests", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void shouldHandleInvalidGuestId() throws Exception {
        mockMvc.perform(get("/api/v1/guests/99999")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void shouldHandleUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/guests/list"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateGuestCreation() throws Exception {
        // Test avec des données invalides
        GuestCreateDto invalidDto = GuestCreateDto.builder()
                .firstName("") // ❌ Nom vide
                .lastName("") // ❌ Nom vide
                .email("invalid-email") // ❌ Email invalide
                .build();

        mockMvc.perform(post("/api/v1/guests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchGuests() throws Exception {
        // Créer quelques guests pour la recherche
        GuestCreateDto searchDto = GuestCreateDto.builder()
                .firstName("Searchable")
                .lastName("User")
                .email("search@email.com")
                .phone("+5555555555")
                .nationality("DE")
                .vipStatus(false)
                .build();

        mockMvc.perform(post("/api/v1/guests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andExpect(status().isCreated());

        // Test de recherche
        mockMvc.perform(post("/api/v1/guests/search")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "searchTerm": "Searchable",
                                "page": 0,
                                "size": 10,
                                "sortBy": "firstName",
                                "sortDirection": "asc"
                            }
                            """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.guests").isArray());
    }
}