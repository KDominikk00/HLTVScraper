package com.example.csstats.application;

import java.util.Optional;

public interface PlayerSnapshotStore {

    Optional<PlayerSnapshot> load();

    void save(PlayerSnapshot snapshot);
}
