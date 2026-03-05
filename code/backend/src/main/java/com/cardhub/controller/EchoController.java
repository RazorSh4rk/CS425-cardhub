package com.cardhub.controller;

import com.cardhub.model.TestData;
import com.cardhub.repository.TestDataRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class EchoController {

    private final TestDataRepository repository;

    public EchoController(TestDataRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/echo")
    public Map<String, String> getEcho() {
        TestData data = repository.findById(1L).orElse(new TestData());
        return Map.of("echo", data.getEcho() != null ? data.getEcho() : "");
    }

    @PostMapping("/echo")
    public Map<String, String> setEcho(@RequestBody Map<String, String> body) {
        TestData data = repository.findById(1L).orElseGet(TestData::new);
        data.setEcho(body.get("echo"));
        repository.save(data);
        return Map.of("echo", data.getEcho());
    }
}
