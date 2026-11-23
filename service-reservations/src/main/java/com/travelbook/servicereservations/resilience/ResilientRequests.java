package com.travelbook.servicereservations.resilience;

import com.travelbook.servicereservations.exception.ServiceExterneIndisponibleException;
import feign.FeignException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ResilientRequests {

    @Retry(name = "#root.args[0]", fallbackMethod = "fallbackLectureOptionnelle")
    @CircuitBreaker(name = "#root.args[0]")
    @Bulkhead(name = "appelsDistants")
    public <T> T lectureOptionnelle(String service, Supplier<T> appel) {
        return appel.get();
    }

    @Retry(name = "#root.args[0]", fallbackMethod = "fallbackLectureObligatoire")
    @CircuitBreaker(name = "#root.args[0]")
    @Bulkhead(name = "appelsDistants")
    public <T> T lectureObligatoire(String service, Supplier<T> appel) {
        return appel.get();
    }

    @CircuitBreaker(name = "#root.args[0]")
    @Bulkhead(name = "appelsDistants")
    public <T> T ecritureObligatoire(String service, Supplier<T> appel) {
        return appel.get();
    }

    private <T> T fallbackLectureOptionnelle(String service, Supplier<T> appel, Throwable e) {
        return null;
    }

    private <T> T fallbackLectureObligatoire(String service, Supplier<T> appel, Throwable e) {
        if (e instanceof FeignException.NotFound notFound) {
            throw notFound;
        }
        if (e instanceof FeignException.Conflict conflict) {
            throw conflict;
        }
        throw new ServiceExterneIndisponibleException(service, e);
    }
}
