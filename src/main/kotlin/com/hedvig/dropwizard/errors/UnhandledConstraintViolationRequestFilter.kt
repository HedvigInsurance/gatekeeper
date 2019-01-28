package com.hedvig.dropwizard.errors

import io.dropwizard.jersey.validation.ValidationErrorMessage
import javax.ws.rs.container.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

@Provider
class UnhandledConstraintViolationRequestFilter : ContainerResponseFilter {
    @Context
    private lateinit var resourceInfo: ResourceInfo
    @Context
    private lateinit var request: Response

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        val resourceClassIsAnnotated = when {
            resourceInfo.resourceClass == null -> false
            else -> resourceInfo.resourceClass.declaredAnnotations.map { it is UnhandledErrorMessages }.contains(true)
        }
        val resourceMethodIsAnnotated = when {
            resourceInfo.resourceMethod == null -> false
            else -> resourceInfo.resourceMethod.declaredAnnotations.map { it is UnhandledErrorMessages }.contains(true)
        }

        if ((resourceClassIsAnnotated || resourceMethodIsAnnotated) && responseContext.entity is ValidationErrorMessage) {
            responseContext.entity = ValidationErrorMessageWrapperMessage(responseContext.entity as ValidationErrorMessage)
        }
    }
}

class ValidationErrorMessageWrapperMessage(val entity: ValidationErrorMessage)
