package ru.webotix.app;

import com.google.inject.AbstractModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import ru.webotix.datasource.database.DatabaseModule;
import ru.webotix.datasource.wiring.WiringModule;
import ru.webotix.exchange.ExchangeModule;
import ru.webotix.job.JobModule;
import ru.webotix.notification.NotificationModule;
import ru.webotix.notification.NotificationModule.SubmissionType;
import ru.webotix.processors.ProcessorModule;
import ru.webotix.websocket.WebSocketModule;

public class WebotixModule extends AbstractModule
        implements Configured<WebotixConfiguration> {

    private final GuiceHibernateModule guiceHibernateModule;

    private WebotixConfiguration configuration;


    public WebotixModule(GuiceHibernateModule guiceHibernateModule) {
        super();
        this.guiceHibernateModule = guiceHibernateModule;
    }

    @Override
    public void setConfiguration(WebotixConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        // Включаем доступ к Hibernate
        install(guiceHibernateModule);

        // Делаем элементы конфигурации доступными для дочерних модулей
        configuration.bind(binder());

        // Открываем веб сокеты
        install(new WebSocketModule());

        install(new DatabaseModule());

        install(new WiringModule());

        install(new JobModule());

        // Регистрируем процессы заданий
        install(new ProcessorModule());

        // Асинхронно пересылает уведомления в Telegram
        install(new NotificationModule(SubmissionType.ASYNC));

        // Регистрируем доступ к заданиям
        install(new ExchangeModule());
    }
}
