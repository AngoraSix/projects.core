package com.angorasix.projects.core.infrastructure.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityConfigurationIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
) {
    @Test
    fun `should allow anonymous access to GET endpoints`() {
        webTestClient
            .get()
            .uri("/projects-core")
            .exchange()
            .expectStatus()
            .isOk // or whatever the real status is if handler is mocked
    }

    @Test
    fun `should require auth for other endpoints`() {
        webTestClient
            .get()
            .uri("/some-other-path")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }
}
