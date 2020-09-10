package ru.webotix.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import ru.webotix.job.spi.StatusUpdate;

public class SynchronousEventStatusUpdateService implements SynchronousStatusUpdateService {

    private final EventBus eventBus;

    @Inject
    public SynchronousEventStatusUpdateService(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public void send(StatusUpdate statusUpdate) {
        eventBus.post(statusUpdate);
    }

}
