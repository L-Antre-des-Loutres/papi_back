package org.antredesloutres.papi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.antredesloutres.papi.util.Constants.API_HEALTH_MESSAGE;

@RestController
public class HealthController {

    @GetMapping({"/", "/health"})
    public String healthCheck() {
        return API_HEALTH_MESSAGE;
    }
}
