package com.travelbook.servicepaiements.resilience;

import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.util.function.Predicate;

public class IgnoreClientErrorsPredicate implements Predicate<Throwable> {

    @Override
    public boolean test(Throwable throwable) {
        if (throwable instanceof FeignException feignException) {
            int status = feignException.status();
            return status >= 400 && status < 500;
        }
        return throwable instanceof BulkheadFullException
                || throwable instanceof CallNotPermittedException
                || throwable instanceof RequestNotPermitted;
    }
}
