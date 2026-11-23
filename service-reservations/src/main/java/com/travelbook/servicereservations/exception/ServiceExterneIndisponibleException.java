package com.travelbook.servicereservations.exception;

public class ServiceExterneIndisponibleException extends RuntimeException {

    public ServiceExterneIndisponibleException(String service, Throwable cause) {
        super("Service externe indisponible : " + service, cause);
    }
}
