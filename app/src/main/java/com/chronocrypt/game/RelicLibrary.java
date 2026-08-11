package com.chronocrypt.game;

import java.util.*;

public final class RelicLibrary {
    private static final LinkedHashMap<String, Relic> RELICS = new LinkedHashMap<>();
    static {
        add("chronometer","Pocket Chronometer","Gain +1 Chrono each turn.","STARTER");
        add("glass_battery","Glass Battery","Gain +1 Energy on turn 1.","COMMON");
        add("archive_key","Archive Key","Past-synced cards gain +2 extra damage/block.","COMMON");
        add("tomorrow_coil","Tomorrow Coil","Forecast damage gains +4.","COMMON");
        add("causal_armor","Causal Armor","Start each combat with 8 Block.","COMMON");
        add("black_archive","Black Archive","Draw +1 card on turn 1.","UNCOMMON");
        add("mending_loop","Mending Loop","Repair 5 HP after every combat.","COMMON");
        add("null_seal","Null Seal","Vent 2 Paradox after every combat.","COMMON");
        add("paradox_lens","Paradox Lens","At 5+ Paradox, attacks deal +3 damage.","UNCOMMON");
        add("redshift_drive","Redshift Drive","Your first era shift each turn is free.","UNCOMMON");
        add("compound_interest","Compound Interest","Gain 20% more Credits from combat.","COMMON");
        add("second_heart","Second Heart","Increase max HP by 12 when acquired.","UNCOMMON");
        add("retrocausal_map","Retrocausal Map","Reveal richer choices: shops and events appear more often.","UNCOMMON");
        add("entropy_sink","Entropy Sink","At turn start, if Paradox >=8, vent 1.","UNCOMMON");
        add("phase_prism","Phase Prism","Synchronized block gains +3.","COMMON");
        add("combat_clock","Combat Clock","Every third turn gain +1 Energy.","UNCOMMON");
        add("mirror_shard","Mirror Shard","Echo damage gains +4.","UNCOMMON");
        add("oracle_eye","Oracle Eye","Future-synced cards draw 1 the first time each turn.","RARE");
        add("old_coin","Old Coin","Gain 120 Credits when acquired.","UNCOMMON");
        add("event_horizon","Event Horizon","Paradox-scaled attacks gain +1 per Paradox.","RARE");
        add("anchor_spike","Anchor Spike","Rewinding costs 1 less Paradox.","RARE");
        add("dead_god_watch","Dead God's Watch","Once per combat, lethal damage leaves you at 1 HP.","RARE");
        add("trinary_core","Trinary Core","Synchronized cards gain +1 damage and +1 block in every era.","RARE");
        add("epoch_crown","Epoch Crown","Gain +1 Energy each turn; enemy attacks gain +2.","BOSS");
    }
    private static void add(String id,String n,String d,String r){RELICS.put(id,new Relic(id,n,d,r));}
    public static Relic get(String id){return RELICS.get(id);}
    public static List<Relic> all(){return new ArrayList<>(RELICS.values());}
    public static Relic random(Random r, List<String> owned){
        ArrayList<Relic> pool=new ArrayList<>();
        for(Relic x:RELICS.values()) if(!x.rarity.equals("STARTER") && !owned.contains(x.id)) pool.add(x);
        return pool.isEmpty()?null:pool.get(r.nextInt(pool.size()));
    }
}
