package com.example.hltvscraper.hltv.application;

import java.util.Optional;

public interface PlayerSnapshotStore {

    Optional<PlayerSnapshot> load();

    void save(PlayerSnapshot snapshot);
}
