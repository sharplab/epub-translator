package net.sharplab.epubtranslator.core.driver.translator

import java.io.Closeable
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MultivaluedMap

@Path("/v2/translate")
interface DeepLClient : Closeable {
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    fun translate(request: MultivaluedMap<String, String>): DeepLTranslateAPIResponse
}
