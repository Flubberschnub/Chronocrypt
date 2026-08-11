package com.chronocrypt.game;

public final class Card {
    public enum Era { PAST, PRESENT, FUTURE }
    public enum Type { ATTACK, SKILL, POWER }

    public final String id;
    public final String name;
    public final String school;
    public final Era era;
    public final Type type;
    public final String rarity;
    public final int cost;
    public final int damage;
    public final int block;
    public final int draw;
    public final int heal;
    public final int paradox;
    public final int shift;
    public final int echoDamage;
    public final int forecastDamage;
    public final int forecastBlock;
    public final int strength;
    public final int dexterity;
    public final int weak;
    public final int vulnerable;
    public final int energy;
    public final int paradoxScale;
    public final boolean exhaust;
    public final boolean retain;
    public final boolean upgraded;

    public Card(String id, String name, String school, Era era, Type type, String rarity,
                int cost, int damage, int block, int draw, int heal, int paradox, int shift,
                int echoDamage, int forecastDamage, int forecastBlock, int strength, int dexterity,
                int weak, int vulnerable, int energy, int paradoxScale, boolean exhaust,
                boolean retain, boolean upgraded) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.era = era;
        this.type = type;
        this.rarity = rarity;
        this.cost = cost;
        this.damage = damage;
        this.block = block;
        this.draw = draw;
        this.heal = heal;
        this.paradox = paradox;
        this.shift = shift;
        this.echoDamage = echoDamage;
        this.forecastDamage = forecastDamage;
        this.forecastBlock = forecastBlock;
        this.strength = strength;
        this.dexterity = dexterity;
        this.weak = weak;
        this.vulnerable = vulnerable;
        this.energy = energy;
        this.paradoxScale = paradoxScale;
        this.exhaust = exhaust;
        this.retain = retain;
        this.upgraded = upgraded;
    }

    public String key() { return upgraded ? id + "#U" : id; }

    public Card upgrade() {
        if (upgraded) return this;
        int nd = damage > 0 ? damage + Math.max(2, damage / 3) : damage;
        int nb = block > 0 ? block + Math.max(2, block / 3) : block;
        int ne = echoDamage > 0 ? echoDamage + 3 : echoDamage;
        int nf = forecastDamage > 0 ? forecastDamage + 4 : forecastDamage;
        int nfb = forecastBlock > 0 ? forecastBlock + 4 : forecastBlock;
        int ndr = draw;
        int nenergy = energy;
        int nstr = strength;
        int ndex = dexterity;
        int ncost = cost;
        if (damage == 0 && block == 0 && echoDamage == 0 && forecastDamage == 0 && forecastBlock == 0) {
            if (draw > 0) ndr++;
            else if (energy > 0) nenergy++;
            else if (strength > 0) nstr++;
            else if (dexterity > 0) ndex++;
            else if (cost > 0) ncost--;
        }
        if (rarity.equals("RARE") && cost >= 2 && damage + block > 18) ncost = Math.max(1, cost - 1);
        return new Card(id, name + "+", school, era, type, rarity, ncost, nd, nb, ndr, heal > 0 ? heal + 3 : heal,
                paradox, shift, ne, nf, nfb, nstr, ndex, weak, vulnerable, nenergy, paradoxScale,
                exhaust, retain, true);
    }

    public String description() {
        StringBuilder s = new StringBuilder();
        if (damage > 0) s.append("Deal ").append(damage).append(". ");
        if (block > 0) s.append("Gain ").append(block).append(" Block. ");
        if (draw > 0) s.append("Draw ").append(draw).append(". ");
        if (heal > 0) s.append("Repair ").append(heal).append(" HP. ");
        if (energy > 0) s.append("Gain ").append(energy).append(" Energy. ");
        if (echoDamage > 0) s.append("Echo ").append(echoDamage).append(" next turn. ");
        if (forecastDamage > 0) s.append("Forecast ").append(forecastDamage).append(" damage. ");
        if (forecastBlock > 0) s.append("Forecast ").append(forecastBlock).append(" Block. ");
        if (strength > 0) s.append("Gain ").append(strength).append(" Force. ");
        if (dexterity > 0) s.append("Gain ").append(dexterity).append(" Phase. ");
        if (weak > 0) s.append("Apply ").append(weak).append(" Weak. ");
        if (vulnerable > 0) s.append("Apply ").append(vulnerable).append(" Exposed. ");
        if (paradox > 0) s.append("Gain ").append(paradox).append(" Paradox. ");
        if (paradox < 0) s.append("Vent ").append(-paradox).append(" Paradox. ");
        if (paradoxScale > 0) s.append("+").append(paradoxScale).append(" damage per Paradox. ");
        if (shift < 0) s.append("Shift toward Past. ");
        if (shift > 0) s.append("Shift toward Future. ");
        if (retain) s.append("Retain. ");
        if (exhaust) s.append("Exhaust. ");
        return s.toString().trim();
    }
}
