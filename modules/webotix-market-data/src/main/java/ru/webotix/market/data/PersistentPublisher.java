package ru.webotix.market.data;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Решение проблемы с быстрым потоком данных
 * Сохранение поточной информации в случае если не успевает обработать слушатель
 *
 * @param <T>
 */
class PersistentPublisher<T> {

    private final Flowable<T> flowable;

    private final AtomicReference<FlowableEmitter<T>>
            emitter = new AtomicReference<>();

    PersistentPublisher() {
        this.flowable =
                setup(
                        Flowable.create(
                                (FlowableEmitter<T> e) -> emitter.set(e.serialize()),
                                BackpressureStrategy.MISSING
                        ).share()
                                .onBackpressureLatest()
                );
    }

    Flowable<T> setup(Flowable<T> base) {
        return base;
    }

    Flowable<T> getAll() {
        return flowable;
    }

    final void emit(T e) {

        FlowableEmitter<T> s = emitter.get();

        if (s != null) {
            s.onNext(e);
        }
    }
}
