package com.lngbaotran.lab306.controller;

import com.lngbaotran.lab306.service.TelemetryStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TelemetryController {

    @Autowired
    private TelemetryStreamService streamService;

    // Endpoint SSE cho React/Flutter subscribe
    @GetMapping(value = "/telemetry/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTelemetry() {
        return streamService.getStream();
    }
}
