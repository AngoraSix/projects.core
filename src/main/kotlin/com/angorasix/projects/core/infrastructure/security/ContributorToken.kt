package com.angorasix.projects.core.infrastructure.security

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.Authentication

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class ContributorToken(
        var contributorId: String,
        var attributes: Map<String, String> = mutableMapOf(),
        var clientPrincipal: Any? = null
) : AbstractAuthenticationToken(emptyList()){
    override fun getCredentials(): Any {
        return ""
    }

    override fun getPrincipal(): Any {
        return this.clientPrincipal!!
    }

}