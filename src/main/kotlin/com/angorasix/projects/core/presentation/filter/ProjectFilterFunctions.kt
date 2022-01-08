package com.angorasix.projects.core.presentation.filter

import com.angorasix.projects.core.infrastructure.config.ServiceConfigs
import com.angorasix.projects.core.infrastructure.security.ContributorToken
import com.angorasix.projects.core.presentation.dto.ContributorHeaderDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.buildAndAwait
import java.util.*

/**
 *
 *
 * @author rozagerardo
 */
suspend fun headerFilterFunction(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse,
        serviceConfigs: ServiceConfigs,
        objectMapper: ObjectMapper
): ServerResponse {
    val contributorHeaderString = Base64.getUrlDecoder().decode(request.headers().header(serviceConfigs.api.contributorHeader).first())
    contributorHeaderString?.let {
        val contributorHeader = objectMapper.readValue(contributorHeaderString, ContributorHeaderDto::class.java)
        val contributorHeaderToken = ContributorToken(contributorHeader.contributorId, contributorHeader.attributes)
        ReactiveSecurityContextHolder.withAuthentication(contributorHeaderToken)
        return next(request)
    }
    return status(HttpStatus.UNAUTHORIZED).buildAndAwait();
}
