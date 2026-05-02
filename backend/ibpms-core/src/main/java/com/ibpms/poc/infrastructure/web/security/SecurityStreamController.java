package com.ibpms.poc.infrastructure.web.security;

import com.ibpms.poc.application.service.security.SecurityStreamService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/security")
public class SecurityStreamController {

    private final SecurityStreamService streamService;

    public SecurityStreamController(SecurityStreamService streamService) {
        this.streamService = streamService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSecurityEvents() {
        return streamService.createEmitter();
    }
}
