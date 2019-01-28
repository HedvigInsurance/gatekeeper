package com.hedvig.dropwizard.messages

import java.io.OutputStream
import java.lang.reflect.Type
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider

@Provider
@Produces(MediaType.TEXT_PLAIN)
class PlainTextMessageBodyWriter : MessageBodyWriter<String> {
    override fun isWriteable(type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType): Boolean {
        return String::class.java.isAssignableFrom(type) && mediaType == MediaType.TEXT_PLAIN_TYPE
    }

    override fun writeTo(t: String, type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType, httpHeaders: MultivaluedMap<String, Any>, entityStream: OutputStream) {
        entityStream.write(t.toByteArray())
    }

    override fun getSize(t: String, type: Class<*>, genericType: Type, annotations: Array<out Annotation>, mediaType: MediaType): Long {
        return -1
    }
}
