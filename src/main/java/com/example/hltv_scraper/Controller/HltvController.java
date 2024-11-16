package com.example.hltv_scraper.Controller;

import com.example.hltv_scraper.Entity.PlayerDTO;
import com.example.hltv_scraper.Service.HltvScraperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hltv")
public class HltvController {

    private final HltvScraperService scraperService;

    public HltvController(HltvScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/players")
    public List<PlayerDTO> getPlayers() {
        try {
            return scraperService.getPlayers(); // Use the caching logic
        } catch (InterruptedException e) {
            e.printStackTrace();
            return List.of(); // Return an empty list or handle errors as needed
        }
    }
}