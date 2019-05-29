package org.servantscode.secureproxy.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/{path:.*}")
public class SecureProxySvc { //extends SCServiceBase {
    private static final Logger LOG = LogManager.getLogger(SecureProxySvc.class);

    @GET
    public Response proxyGet(@Context HttpServletRequest request,
                             @Context HttpHeaders headers,
                             @PathParam("path") String path) {
//        if(!userHasAccess("kibana.read"))
//            return Response.temporaryRedirect(URI.create(request.getLocalName() + ":4200/login")).build();

        String url = request.getRequestURI();
        String query = request.getQueryString();
        String reqString = url + (isSet(query)? "?" + query: "");

        LOG.debug("Proxying: " + reqString);

        WebTarget webTarget = ClientBuilder.newClient(new ClientConfig().register(SecureProxySvc.class))
                .target("http://kibana-lb:5601");
        WebTarget target = webTarget.path(reqString);
        LOG.debug("to: " + target.getUri());

        MultivaluedMap headerMap = headers.getRequestHeaders();
        headerMap.remove("Authorization");
        Response resp = target.request().headers(headerMap).get();
        return Response.fromResponse(resp).build();
    }

    private boolean isSet(String str) {
        return str != null && str.length() > 0;
    }
}
