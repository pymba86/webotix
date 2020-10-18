package ru.webotix.script;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
import io.github.resilience4j.core.StringUtils;
import ru.webotix.job.JobResource;
import ru.webotix.market.data.api.TickerSpec;
import ru.webotix.utils.Hasher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ScriptResource implements WebResource {

    private final JobResource jobResource;
    private final Hasher hasher;
    private final ScriptConfiguration config;
    private final ScriptAccess scriptAccess;

    @Inject
    ScriptResource(
            JobResource jobResource,
            ScriptAccess scriptAccess,
            ScriptConfiguration configuration,
            Hasher hasher) {
        this.jobResource = jobResource;
        this.scriptAccess = scriptAccess;
        this.config = configuration;
        this.hasher = hasher;
    }

    @PUT
    @Timed
    @Path("/scripts/{id}")
    @UnitOfWork
    public Response putScript(@PathParam("id") String id, Script script) {

        if (!id.equals(script.id())) {
            return Response.status(400)
                    .entity(ImmutableMap.of("error", "id doesn't match endpoint"))
                    .build();
        }

        scriptAccess.saveOrUpdate(script);

        return Response.ok().build();
    }

    @DELETE
    @Timed
    @Path("/scripts/{id}")
    @UnitOfWork
    public void deleteScript(@PathParam("id") String id) {
        scriptAccess.delete(id);
    }

    @GET
    @Timed
    @Path("/scripts")
    @UnitOfWork(readOnly = true)
    public Iterable<Script> listScripts() {
        return scriptAccess.list();
    }

    @PUT
    @Timed
    @Path("/scriptjobs/{id}")
    @UnitOfWork
    public Response putJob(@PathParam("id") String id, ScriptJobPrototype job) {
        return jobResource.put(
                id,
                ScriptJob.builder()
                        .id(job.id)
                        .name(job.name)
                        .ticker(job.ticker)
                        .script(job.script)
                        .scriptHash(
                                StringUtils.isNotEmpty(config.getScriptSigningKey())
                                        ? hasher.hashWithString(job.script, config.getScriptSigningKey())
                                        : ScriptAccess.UNSIGNED)
                        .build());
    }

    public static final class ScriptJobPrototype {

        @JsonProperty
        private String id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String script;

        @JsonProperty
        private TickerSpec ticker;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = script;
        }

        public TickerSpec getTicker() {
            return ticker;
        }

        public void setTicker(TickerSpec ticker) {
            this.ticker = ticker;
        }
    }
}