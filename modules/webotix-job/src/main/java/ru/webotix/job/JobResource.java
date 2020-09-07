package ru.webotix.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
import org.apache.commons.lang3.StringUtils;
import ru.webotix.job.spi.Job;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class JobResource implements WebResource {

    private final JobSubmitter jobSubmitter;
    private final JobAccess jobAccess;

    @Inject
    JobResource(JobAccess jobAccess, JobSubmitter jobSubmitter) {
        this.jobAccess = jobAccess;
        this.jobSubmitter = jobSubmitter;
    }

    @GET
    @Timed
    @UnitOfWork(readOnly = true)
    public Collection<Job> list() {
        return ImmutableList.copyOf(jobAccess.list());
    }

    @PUT
    @Timed
    @Path("/{id}")
    @UnitOfWork
    public Response put(@PathParam("id") String id, Job job) {
        if (StringUtils.isEmpty(job.id()) || !job.id().equals(id)) {

            return Response.status(400)
                    .entity(ImmutableMap.of("error",
                            "id not set or query and body do not match"))
                    .build();
        }

        jobSubmitter.submitNewUnchecked(job);

        return Response.ok().build();
    }

    @POST
    @Timed
    @UnitOfWork
    public Job post(Job job) {
        return jobSubmitter.submitNewUnchecked(job);
    }

    @DELETE
    @Timed
    @UnitOfWork
    public void deleteAllJobs() {
        jobAccess.deleteAll();
    }

    @GET
    @Path("{id}")
    @Timed
    @UnitOfWork(readOnly = true)
    public Response fetchJob(@PathParam("id") String id) {
        try {
            return Response.ok()
                    .entity(jobAccess.load(id))
                    .build();
        } catch (JobAccess.JobDoesNotExistException e) {
            return Response.status(404).build();
        }
    }
}
