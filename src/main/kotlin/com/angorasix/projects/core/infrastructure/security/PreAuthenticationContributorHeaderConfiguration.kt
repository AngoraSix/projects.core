package com.angorasix.projects.core.infrastructure.security

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class PreAuthContributorHeaderProcessingFilter: AbstractPreAuthenticatedProcessingFilter() {
    val principalRequestHeader = "SM_USER"
    val credentialsRequestHeader: String? = null
    val exceptionIfHeaderMissing = true

    fun PreAuthContributorHeaderProcessingFilter() {}

//    fun getPreAuthenticatedPrincipal(request: HttpServletRequest): Any? {
//        val principal: String = request.getHeader(this.principalRequestHeader)
//        return if (principal == null && this.exceptionIfHeaderMissing) {
//            throw PreAuthenticatedCredentialsNotFoundException(this.principalRequestHeader + " header not found in request.")
//        } else {
//            principal
//        }
//    }
//
//    fun getPreAuthenticatedCredentials(request: HttpServletRequest): Any? {
//        return if (this.credentialsRequestHeader != null) request.getHeader(this.credentialsRequestHeader) else "N/A"
//    }
//
//    fun setPrincipalRequestHeader(principalRequestHeader: String) {
//        org.springframework.util.Assert.hasText(principalRequestHeader, "principalRequestHeader must not be empty or null")
//        this.principalRequestHeader = principalRequestHeader
//    }
//
//    fun setCredentialsRequestHeader(credentialsRequestHeader: String) {
//        org.springframework.util.Assert.hasText(credentialsRequestHeader, "credentialsRequestHeader must not be empty or null")
//        this.credentialsRequestHeader = credentialsRequestHeader
//    }
//
//    fun setExceptionIfHeaderMissing(exceptionIfHeaderMissing: Boolean) {
//        this.exceptionIfHeaderMissing = exceptionIfHeaderMissing
//    }
}