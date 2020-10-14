package ru.webotix.app;

import com.google.inject.AbstractModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import ru.webotix.base.SubmissionType;
import ru.webotix.datasource.database.DatabaseModule;
import ru.webotix.datasource.wiring.WiringModule;
import ru.webotix.exchange.ExchangeModule;
import ru.webotix.job.JobModule;
import ru.webotix.job.status.JobStatusModule;
import ru.webotix.market.data.MarketDataModule;
import ru.webotix.notification.NotificationModule;
import ru.webotix.processors.ProcessorModule;
import ru.webotix.subscription.SubscriptionModule;
import ru.webotix.telegram.TelegramModule;
import ru.webotix.websocket.WebSocketModule;

public class WebotixModule extends AbstractModule
        implements Configured<WebotixConfiguration> {

    private final GuiceHibernateModule guiceHibernateModule;

    private WebotixConfiguration configuration;

    WebotixModule(GuiceHibernateModule guiceHibernateModule) {
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

        // Настройка базы данных
        install(new DatabaseModule());

        // Настройка схем подключения
        install(new WiringModule());

        // Управление заданиями
        install(new JobModule());

        // Регистрируем процессы заданий
        install(new ProcessorModule());

        // Управление подписками на тикеты
        install(new SubscriptionModule());

        // Управление уведомлениями
        install(new NotificationModule(SubmissionType.SYNC));

        // Управление статусами заданий
        install(new JobStatusModule(SubmissionType.SYNC));

        // Регистрируем отправку уведомлений в telegram
        install(new TelegramModule());

        // Регистрируем доступ к заданиям
        install(new ExchangeModule());

        // Управление доступом к рыночным данных
        install(new MarketDataModule());
    }
}
