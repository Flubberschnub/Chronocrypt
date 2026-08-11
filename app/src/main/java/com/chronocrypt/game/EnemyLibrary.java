package com.chronocrypt.game;

import java.util.*;

public final class EnemyLibrary {
    private static final String[][] NORMAL = {
            {"rusted_scout","Rusted Scout","abandoned war drone"},
            {"memory_leech","Memory Leech","predator of discarded histories"},
            {"causal_raider","Causal Raider","pirate from a victorious future"},
            {"clockwork_hound","Clockwork Hound","hunts across adjacent seconds"},
            {"archive_ghost","Archive Ghost","a soldier whose war never happened"},
            {"phase_marauder","Phase Marauder","half a heartbeat out of phase"},
            {"entropy_monk","Entropy Monk","worships the heat death"},
            {"future_widow","Future Widow","avenges a death not yet committed"},
            {"null_engineer","Null Engineer","repairs reality by deleting witnesses"}
    };
    private static final String[][] ELITE = {
            {"bootstrap_knight","Bootstrap Knight","its armor has no inventor"},
            {"chronal_hydra","Chronal Hydra","each head remembers a different outcome"},
            {"retrograde_angel","Retrograde Angel","falls upward through history"}
    };

    public static Enemy random(Random r,int act,boolean elite){
        String[] e=(elite?ELITE:NORMAL)[r.nextInt(elite?ELITE.length:NORMAL.length)];
        int hp=(elite?92:58)+(act-1)*(elite?28:18)+r.nextInt(15);
        int dmg=(elite?12:8)+(act-1)*3+r.nextInt(3);
        int pattern=r.nextInt(5)+1;
        return new Enemy(e[0],e[1],e[2],hp,dmg,pattern,elite,false);
    }

    public static Enemy boss(int act){
        if(act==1) return new Enemy("chronophage","THE CHRONOPHAGE","devours the seconds behind you",180,13,3,false,true);
        if(act==2) return new Enemy("retro_oracle","THE RETROGRADE ORACLE","remembers every future you refuse",260,16,4,false,true);
        return new Enemy("first_last_machine","THE FIRST / LAST MACHINE","built at both ends of civilization",360,19,5,false,true);
    }
}
