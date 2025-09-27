package com.tvboot.tivio.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extension JUnit pour configurer le profil de test
 */
public class TestProfileJUnitExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        System.setProperty("spring.profiles.active", "test");
    }
}