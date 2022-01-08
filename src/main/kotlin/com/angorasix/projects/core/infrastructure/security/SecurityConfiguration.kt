package com.angorasix.projects.core.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.http
import org.springframework.security.converter.RsaKeyConverters.x509
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.x509.SubjectDnX509PrincipalExtractor
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono


/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@Configuration
class SecurityConfiguration {

    @Bean
    fun authProvider(): ReactiveAuthenticationManager {
        return ReactiveAuthenticationManager { authentication: Authentication ->
            authentication.isAuthenticated = "Trusted Org Unit" == authentication.name
            Mono.just(authentication)
        }
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        val customPrincipalExtractor = SubjectDnX509PrincipalExtractor()
        customPrincipalExtractor.setSubjectDnRegex("OU=(.*?)(?:,|$)")
        val customAuthenticationManager = ReactiveAuthenticationManager { authentication: Authentication ->
            authentication.isAuthenticated = "Trusted Org Unit" == authentication.name
            Mono.just(authentication)
        }
        return http {
            x509 {
                principalExtractor = customPrincipalExtractor
                authenticationManager = customAuthenticationManager
            }
            authorizeExchange {
                authorize(anyExchange, authenticated)
            }
        }
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): ServerHttpSecurity? {
        val customAuthenticationManager = ReactiveAuthenticationManager { authentication: Authentication ->
            authentication.isAuthenticated = "Trusted Org Unit" == authentication.name
            Mono.just(authentication)
        }

        return http.authorizeExchange { it.anyExchange().authenticated() }
                .authenticationManager(customAuthenticationManager)
                .deta

//        {
//            authorizeExchange {
//                authorize("/message/**", hasAuthority("SCOPE_message:read"))
//                authorize(anyExchange, authenticated)
//            }
//        }
    }
}