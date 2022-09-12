package com.angorasix.projects.core.domain.project

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
data class ContributorDetails(
    var contributorId: String,
    var attributes: Map<String, String> = mutableMapOf(),
)
