package com.stayfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punctul de intrare al aplicației.
 * @SpringBootApplication activează auto-configurarea și scanarea componentelor
 * din pachetul com.stayfinder și subpachetele lui (controller, service, repository...).
 */
@SpringBootApplication
public class StayFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(StayFinderApplication.class, args);
    }
}
