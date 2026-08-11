package com.chronocrypt.game;

public final class Enemy {
    public final String id;
    public final String name;
    public final String subtitle;
    public final int maxHp;
    public int hp;
    public int baseDamage;
    public int strength;
    public int weak;
    public int vulnerable;
    public int pattern;
    public int turn;
    public boolean elite;
    public boolean boss;

    public Enemy(String id, String name, String subtitle, int hp, int damage, int pattern, boolean elite, boolean boss) {
        this.id=id; this.name=name; this.subtitle=subtitle; this.maxHp=hp; this.hp=hp;
        this.baseDamage=damage; this.pattern=pattern; this.elite=elite; this.boss=boss;
    }

    public int intentDamage(int paradox) {
        int d = baseDamage + strength;
        if (pattern == 1 && turn % 3 == 2) d += 5;
        if (pattern == 2 && turn % 2 == 1) d = Math.max(1, d - 3);
        if (pattern == 3 && turn % 4 == 3) d += 8;
        if (pattern == 4 && turn % 3 == 0) d = 0;
        if (pattern == 5 && turn % 5 == 4) d += 12;
        if (paradox >= 6) d += 2;
        if (weak > 0) d = (int)Math.floor(d * 0.75f);
        return Math.max(0, d);
    }

    public String intentText(int paradox) {
        int d = intentDamage(paradox);
        if (pattern == 4 && turn % 3 == 0) return "CALIBRATE • gains Force";
        if (pattern == 2 && turn % 2 == 1) return "SIPHON • " + d + " + Paradox";
        if (pattern == 3 && turn % 4 == 3) return "RUPTURE • " + d;
        if (pattern == 5 && turn % 5 == 4) return "ERASE • " + d;
        return "ATTACK • " + d;
    }
}
