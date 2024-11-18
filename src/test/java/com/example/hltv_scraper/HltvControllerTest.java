package com.example.hltv_scraper;

import com.example.hltv_scraper.Controller.HltvController;
import com.example.hltv_scraper.Entity.PlayerDTO;
import com.example.hltv_scraper.Service.HltvScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(HltvController.class)
public class HltvControllerTest {

    @Mock
    private HltvScraperService scraperService;

    @InjectMocks
    private HltvController hltvController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(hltvController).build();
    }

    @Test
    public void testGetPlayers() throws Exception {
        List<PlayerDTO> mockPlayers = List.of(
                new PlayerDTO("Player1", "Team1", "logoUrl1", "50", "100", "10", "1.5", "1.2")
        );

        // Mock the service call to return the mocked data
        when(scraperService.getPlayers()).thenReturn(mockPlayers);

        mockMvc.perform(get("/api/hltv/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].playerName").value("Player1"))
                .andExpect(jsonPath("$[0].team").value("Team1"))
                .andExpect(jsonPath("$[0].rating").value("1.2"));
    }
}