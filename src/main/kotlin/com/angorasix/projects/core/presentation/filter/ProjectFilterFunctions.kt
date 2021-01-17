package com.angorasix.projects.core.presentation.filter

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

/**
 *
 *
 * @author rozagerardo
 */
suspend fun headerFilterFunction(
    request: ServerRequest,
    next: suspend (ServerRequest) -> ServerResponse
): ServerResponse {
    request.attributes()
        .put(
            "ger1",
            request.headers()
                .header("Angorasix-API")
        )
    return next(request)
}
