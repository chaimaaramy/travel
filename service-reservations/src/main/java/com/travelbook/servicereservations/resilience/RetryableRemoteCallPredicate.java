package com.travelbook.servicereservations.resilience;

import feign.FeignException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class RetryableRemoteCallPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof FeignException feignException) {
            int status = feignException.status();
            return status < 0 || status >= 500;
        }
        return throwable instanceof IOException || throwable instanceof TimeoutException;
    }
}
