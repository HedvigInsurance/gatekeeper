package com.hedvig.dropwizard.pebble

import com.mitchellbosecke.pebble.PebbleEngine
import org.slf4j.LoggerFactory.getLogger
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.Exception
import java.lang.reflect.Type
import java.nio.charset.Charset
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response
import javax.ws.rs.ext.MessageBodyWriter
import javax.ws.rs.ext.Provider

@Provider
@Produces(MediaType.TEXT_HTML)
class PebbleMessageBodyWriter(
    private val engine: PebbleEngine,
    private val prefix: String
) : MessageBodyWriter<View> {
    private val LOG = getLogger(PebbleMessageBodyWriter::class.java)

    override fun isWriteable(clazz: Class<*>, type: Type, annotations: Array<out Annotation>, mediaType: MediaType): Boolean {
        return View::class.java.isAssignableFrom(clazz)
    }

    override fun writeTo(view: View, clazz: Class<*>, type: Type, annotations: Array<out Annotation>, mediaType: MediaType, attributes: MultivaluedMap<String, Any>, outputStream: OutputStream) {
        try {
            val outputStreamWriter = OutputStreamWriter(outputStream, Charset.forName("UTF-8"))
            engine.getTemplate(prefix + view.getName()).evaluate(outputStreamWriter, view.getContext())
        } catch (e: Exception) {
            LOG.error("Failed to render pebble template", e)
            throw WebApplicationException(
                e,
                Response.serverError()
                    .type(MediaType.TEXT_PLAIN)
                    .entity("""
There was an error rendering the template:
${e.message}
    ${e.stackTrace.joinToString("\n    ")}
                        """.trimIndent())
                    .build()
            )
        }
    }

    override fun getSize(view: View, clazz: Class<*>, type: Type, annotations: Array<out Annotation>, mediaTYpe: MediaType): Long {
        return -1
    }
}
