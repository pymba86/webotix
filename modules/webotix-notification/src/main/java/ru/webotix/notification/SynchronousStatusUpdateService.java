package ru.webotix.notification;

import com.google.inject.ImplementedBy;
import ru.webotix.job.spi.StatusUpdateService;

@ImplementedBy(SynchronousEventStatusUpdateService.class)
public interface SynchronousStatusUpdateService extends StatusUpdateService {
}
