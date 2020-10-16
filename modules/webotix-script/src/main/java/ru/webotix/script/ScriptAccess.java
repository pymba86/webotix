package ru.webotix.script;

import com.google.common.collect.Maps;
import com.google.inject.Provider;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.utils.Hasher;

import static java.util.stream.Collectors.toList;
import static ru.webotix.script.Script.TABLE_NAME;

class ScriptAccess {

    static final String UNSIGNED = "UNSIGNED";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptAccess.class);

    private final Provider<SessionFactory> sessionFactory;
    private final Hasher hasher;
    private final ScriptConfiguration config;

    @Inject
    ScriptAccess(Provider<SessionFactory> sf, Hasher hasher, ScriptConfiguration config) {
        this.sessionFactory = sf;
        this.hasher = hasher;
        this.config = config;
    }

    void saveOrUpdate(Script script) {
        if (StringUtils.isNotEmpty(config.getScriptSigningKey())) {
            script.setScriptHash(hasher.hashWithString(script.script(), config.getScriptSigningKey()));
        } else {
            script.setScriptHash(UNSIGNED);
        }
        script.parameters().forEach(p -> p.setParent(script));
        LOGGER.debug("Saving script: {}", script);

        List<String> parameterNames =
                script.parameters().stream().map(ScriptParameter::name).collect(toList());
        if (parameterNames.isEmpty()) {
            session()
                    .createQuery(
                            "delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :scriptId")
                    .setParameter("scriptId", script.id())
                    .executeUpdate();
        } else {
            session()
                    .createQuery(
                            "delete from "
                                    + ScriptParameter.TABLE_NAME
                                    + " where id.scriptId = :scriptId and id.name not in :names")
                    .setParameter("scriptId", script.id())
                    .setParameterList("names", parameterNames)
                    .executeUpdate();
        }

        script.parameters().forEach(p -> session().saveOrUpdate(p));
        session().saveOrUpdate(script);
    }

    Iterable<Script> list() {
        Map<String, Script> scripts =
                Maps.uniqueIndex(
                        session().createQuery("from " + TABLE_NAME, Script.class).stream()
                                .filter(this::scriptValid)
                                .collect(toList()),
                        Script::id);
        session()
                .createQuery("from " + ScriptParameter.TABLE_NAME, ScriptParameter.class)
                .list()
                .forEach(
                        p -> {
                            Script script = scripts.get(p.scriptId());
                            if (script == null) {
                                LOGGER.warn("Ophaned parameter: {}", p);
                            } else {
                                script.parameters().add(p);
                            }
                        });
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Loaded scripts: {}", scripts.values());
        return scripts.values();
    }

    private boolean scriptValid(Script s) {
        if (StringUtils.isEmpty(config.getScriptSigningKey())) return true;
        boolean valid =
                hasher.hashWithString(s.script(), config.getScriptSigningKey()).equals(s.scriptHash());
        if (!valid)
            LOGGER.warn(
                    "Ignoring script [{}] since script hash mismatches. Possible DB intrusion?", s.id());
        return valid;
    }

    void delete(String id) {
        session()
                .createQuery("delete from " + TABLE_NAME + " where id = :id")
                .setParameter("id", id)
                .executeUpdate();
        session()
                .createQuery("delete from " + ScriptParameter.TABLE_NAME + " where id.scriptId = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    private Session session() {
        return sessionFactory.get().getCurrentSession();
    }
}