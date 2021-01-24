package ru.webotix.auth;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import ru.webotix.auth.jwt.JwtModule;

import java.util.Optional;

public class AuthModule extends AbstractModule {

    public static final String BIND_ACCESS_TOKEN_KEY = "accessToken";
    public static final String BIND_ROOT_PATH = "auth-rootPath";
    public static final String BIND_WEBSOCKET_ENTRY_POINT = "auth-ws-entry";

    private final AuthConfiguration configuration;

    public AuthModule(AuthConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        if (configuration != null) {
            install(new JwtModule(configuration));
            Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
                    .addBinding()
                    .to(AuthEnvironment.class);
            install(new Testing());
        }
    }

    public static final class Testing extends AbstractModule {
        @Override
        protected void configure() {
            bind(new TypeLiteral<Optional<String>>() {})
                    .annotatedWith(Names.named(BIND_ACCESS_TOKEN_KEY))
                    .toProvider(AccessTokenProvider.class)
                    .in(RequestScoped.class);
        }
    }
}
