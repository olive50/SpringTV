
package com.tvboot.tivio.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvboot.tivio.config.TestConfig;
import com.tvboot.tivio.config.TestProfileJUnitExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de base pour tous les tests d'intégration
 * Fournit une configuration commune et des utilitaires partagés
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestConfig.class)
@ExtendWith(TestProfileJUnitExtension.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Utilitaire pour l'authentification dans les tests
     */
    protected String authenticateAndGetToken(String username, String password) throws Exception {
        // Implementation de l'authentification commune
        return "mock-token-for-tests";
    }

    /**
     * Utilitaire pour créer des headers d'authentification
     */
    protected String createAuthHeader(String token) {
        return "Bearer " + token;
    }
}