// @Traceability: US-005, CA-78 Sandbox Multi-tenancy
package com.ibpms.poc.infrastructure.web;

import com.ibpms.poc.infrastructure.web.annotation.SandboxOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/design/sandbox")
public class SandboxController {

    @PostMapping("/workers/execute-mock")
    @SandboxOperation
    public ResponseEntity<Map<String, Object>> executeMock(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
                "status", "mocked",
                "real_api_called", false
        ));
    }
}
