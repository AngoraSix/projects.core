package com.angorasix.projects.core.presentation.filter

import com.angorasix.projects.core.domain.project.ContributorDetails
import com.angorasix.projects.core.infrastructure.config.ServiceConfigs
import com.angorasix.projects.core.presentation.dto.ContributorHeaderDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
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
    request.headers().header(serviceConfigs.api.contributorHeader).firstOrNull()?.let {
        val contributorHeaderString = Base64.getUrlDecoder().decode(it)
        val contributorHeader = objectMapper.readValue(contributorHeaderString, ContributorHeaderDto::class.java)
        val contributorToken = ContributorDetails(contributorHeader.contributorId, contributorHeader.attributes)
        request.attributes()[serviceConfigs.api.contributorHeader] = contributorToken
        return next(request)
    }
    return status(HttpStatus.UNAUTHORIZED).buildAndAwait();
}
