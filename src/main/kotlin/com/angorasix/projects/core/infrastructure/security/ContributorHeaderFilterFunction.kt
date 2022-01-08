package com.angorasix.projects.core.infrastructure.security

import com.angorasix.projects.core.presentation.dto.ContributorHeaderDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ContributorHeaderFilterFunction(private val objectMapper: ObjectMapper) : HandlerFilterFunction<ServerResponse, ServerResponse> {

    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
        val contributorHeaderString = Base64.getUrlDecoder().decode(request.headers().header(headerName).first())
        val contributorHeader = objectMapper.readValue(contributorHeaderString, ContributorHeaderDto::class.java)
        return contributorHeader.contributorId


        if (serverRequest.pathVariable("name").equalsIgnoreCase("test")) {
            return ServerResponse.status(FORBIDDEN).build();
        }
        return handlerFunction.handle(serverRequest);
    }
}