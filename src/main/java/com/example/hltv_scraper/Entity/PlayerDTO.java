package com.example.hltv_scraper.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerDTO {
    private String playerName;
    private String team;
    private String teamLogo;
    private String maps;
    private String rounds;
    private String kddiff;
    private String kd;
    private String rating;
}