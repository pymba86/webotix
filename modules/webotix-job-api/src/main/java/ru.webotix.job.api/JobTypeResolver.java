package ru.webotix.job.api;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class JobTypeResolver extends TypeIdResolverBase {

    private static final Logger log = LoggerFactory.getLogger(JobTypeResolver.class);

    private static final Map<String, Class<? extends Job>> registered = loadJobClasses();

    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        Class<? extends Job> clazz = registered.get(id);
        if (clazz == null) {
            throw new IllegalArgumentException("Job type '" + id + "' not found");
        }

        return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
    }

    @Override
    public String idFromValueAndType(Object o, Class<?> clazz) {
        return clazz.getSimpleName()
                .replaceAll("AutoValue_", "");
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "";
    }

    @Override
    public Id getMechanism() {
        return Id.CUSTOM;
    }

    private static Map<String, Class<? extends Job>> loadJobClasses() {

        List<Class<? extends Job>> jobs = getSubclasses()
                .stream()
                .filter(c -> {
                    final int m = c.getModifiers();
                    return !(Modifier.isAbstract(m) || Modifier.isInterface(m));
                })
                .collect(Collectors.toList());

        try {

            ImmutableMap<String, Class<? extends Job>> result =
                    jobs.stream()
                            .collect(ImmutableMap
                                    .toImmutableMap(
                                            p -> p.getSimpleName()
                                                    .replaceAll("AutoValue_", ""),
                                            p -> p));

            log.debug("Job types registered: {}", result);

            return result;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Duplication job names registered", e);
        }
    }

    private static ArrayList<Class<? extends Job>> getSubclasses() {
        return new ArrayList<>(
                new Reflections("ru.webotix.processors")
                        .getSubTypesOf(Job.class));
    }
}
