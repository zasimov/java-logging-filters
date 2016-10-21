package com.cyberdyber.filters;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface TestClient {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello();

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("send")
    public String supersend(Eugene e);
}
