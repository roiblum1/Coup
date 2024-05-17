package com.example.demo6.AI;

import java.util.HashMap;
import java.util.Map;

class TranspositionTable {
    private Map<Long, TranspositionEntry> table;

    public TranspositionTable() {
        table = new HashMap<>();
    }

    public void store(long hash, TranspositionEntry entry) {
        table.put(hash, entry);
    }

    public TranspositionEntry lookup(long hash) {
        return table.get(hash);
    }
}

