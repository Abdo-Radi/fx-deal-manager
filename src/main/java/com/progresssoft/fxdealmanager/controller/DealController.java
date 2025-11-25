package com.progresssoft.fxdealmanager.controller;

import com.progresssoft.fxdealmanager.service.DealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/deals")
public class DealController {

    private final DealService dealService;

    @Autowired
    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @PostMapping("/import")
    public ResponseEntity<List<String>> importDeals(@RequestParam("file") MultipartFile file) {
        try {
            List<String> log = dealService.importDealsFromExcel(file.getInputStream());
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of("File upload failed: " + e.getMessage()));
        }
    }
}

