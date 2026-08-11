package com.chronocrypt.game;

public final class Relic {
    public final String id;
    public final String name;
    public final String description;
    public final String rarity;
    public Relic(String id, String name, String description, String rarity) {
        this.id = id; this.name = name; this.description = description; this.rarity = rarity;
    }
}
