package com.hedvig.dropwizard.errors

import java.io.OutputStream
import java.lang.reflect.Type
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider

@Provider
@Produces(MediaType.TEXT_HTML)
class ValidationErrorMessageBodyWriter : MessageBodyWriter<ValidationErrorMessageWrapperMessage> {
    override fun isWriteable(type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType): Boolean {
        return ValidationErrorMessageWrapperMessage::class.java.isAssignableFrom(type) && mediaType == MediaType.TEXT_HTML_TYPE
    }

    override fun writeTo(t: ValidationErrorMessageWrapperMessage, type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType, httpHeaders: MultivaluedMap<String, Any>, entityStream: OutputStream) {
        entityStream.write("""
<html>
<head>
<title>Error when validating request</title>
</head>
<body>
<h1>Hm, something isn't right with that request:</h1>
<ul>
        """.trimIndent().toByteArray())
        entityStream.write(t.entity.errors.joinToString("\n") { "<li>$it</li>" }.toByteArray())
        entityStream.write("""
</ul>
</body>
</html>
        """.trimIndent().toByteArray())
    }

    override fun getSize(t: ValidationErrorMessageWrapperMessage, type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType): Long {
        return -1
    }
}