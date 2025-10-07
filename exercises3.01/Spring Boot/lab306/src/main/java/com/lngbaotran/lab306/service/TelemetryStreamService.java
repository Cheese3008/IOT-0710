// package com.lngbaotran.lab306.service;

// import org.springframework.stereotype.Service;
// import reactor.core.publisher.Flux;
// import reactor.core.publisher.Sinks;

// @Service
// public class TelemetryStreamService {

//     private final Sinks.Many<String> sink;

//     public TelemetryStreamService() {
//         // Cho phép nhiều client cùng subscribe
//         this.sink = Sinks.many().multicast().onBackpressureBuffer();
//     }

//     /** Client React/Flutter subscribe vào luồng này */
//     public Flux<String> getStream() {
//         return sink.asFlux();
//     }

//     /** Gửi dữ liệu mới tới tất cả client */
//     public void send(String payload) {
//         sink.tryEmitNext(payload);
//     }
// }

package com.lngbaotran.lab306.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class TelemetryStreamService {

    // Sử dụng Sinks để phát dữ liệu realtime
    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(String data) {
        sink.tryEmitNext(data);
    }

    public Flux<String> getStream() {
        return sink.asFlux();
    }
}
