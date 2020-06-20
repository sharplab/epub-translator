package net.sharplab.epubtranslator.core.driver.translator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

@Path("/v2/translate")
public interface DeepLTranslatorClient {

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    DeepLTranslationResponse translate(MultivaluedMap<String, String> request);
}
