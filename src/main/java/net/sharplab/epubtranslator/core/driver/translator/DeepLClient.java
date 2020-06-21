package net.sharplab.epubtranslator.core.driver.translator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import java.io.Closeable;

@Path("/v2/translate")
public interface DeepLClient extends Closeable {

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    DeepLTranslateAPIResponse translate(MultivaluedMap<String, String> request);
}
