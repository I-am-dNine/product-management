package com.company.product.demo.controller;

import com.company.product.demo.repository.HealthRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthRepository repo;

    public HealthController(HealthRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String health() {
        return "OK-" + repo.ping();
    }
}
