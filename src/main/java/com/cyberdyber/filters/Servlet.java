package com.cyberdyber.filters;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;


@Path("/")
public class Servlet {
    private static final Logger LOGGER = Logger.getLogger(
            ContainerLoggingFilter.class.getName());

    @POST
    @Produces("text/html")
    public Response hello(@Context HttpServletRequest request, String input) {
        ResteasyClient client;
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
                .register(new ClientLoggingFilter());
        client = clientBuilder.build();
        TestClient testClient = client.target("http://localhost:5008").proxy(TestClient.class);

        LOGGER.info("handler call");
        LOGGER.info(input);

        Eugene e = new Eugene("eugene");
        String answer = testClient.hello();
        answer += testClient.supersend(e);

        return Response.status(200).entity(answer).build();
    }
}