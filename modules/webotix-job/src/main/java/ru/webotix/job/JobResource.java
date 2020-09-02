package ru.webotix.job;

import javax.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {


}
