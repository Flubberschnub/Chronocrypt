package com.chronocrypt.game;

import java.util.*;
import static com.chronocrypt.game.Card.Era;
import static com.chronocrypt.game.Card.Type;

public final class CardLibrary {
    private static final LinkedHashMap<String, Card> CARDS = new LinkedHashMap<>();
    private static final String[] SCHOOLS = {"ARCHIVE", "VANGUARD", "ORACLE", "ENTROPY", "WEAVER", "NULL"};
    private static final String[][] NAMES = {
            {"Mnemonic Bolt","Ancestor Guard","Recovered Moment","Dead Letter","Echo Chamber","First Cause","Pale Memory","Archive Dive","Inheritance","Carbon Copy","Lost Battalion","Old Wound","Recorded Thunder","Fossil Signal","Before Image","Precedent","Forgotten Door","Origin Code","Memory Palace","Yesterday's Knife"},
            {"Pulse Strike","Phase Guard","Now or Never","Hard Sync","Overclock","Kinetic Proof","Live Wire","Moment Armory","Deadline","Vector Break","Current Affairs","Flashpoint","Present Tense","Combat Clock","Zero Latency","Center Mass","Stand Fast","Instant Replay","Pressure Window","Redline"},
            {"Tomorrow's Weapon","Probability Shell","Foreseen Opening","Distant Thunder","Promised Violence","Second Sunrise","Outcome Lock","Prophecy Engine","Next Iteration","Terminal Bloom","Deferred Payment","Future Perfect","Horizon Lance","Long Bet","Scheduled Collapse","Afterimage","Forecast Barrier","Destiny Leak","End State","Last Word"},
            {"Contradiction","Paradox Skin","Impossible Angle","Causality Tax","Entropy Loan","Grandfather Clause","Unmade Shot","Broken Continuum","Acausal Hunger","Schrodinger's Trigger","Ruin Interest","Split History","Bootstrap Blade","Heat Death","False Memory","Causal Debt","Fracture Bloom","Error Budget","Twin Outcome","Beautiful Disaster"},
            {"Loopstep","Thread Shield","Slipstream","Era Stitch","Borrowed Time","Crossfade","Splice Point","Temporal Feint","Clockwork Vein","Threefold Path","Backstep","Fast Forward","Timeline Braid","Continuity","Second Hand","Phase Transit","Moment Swap","Chrono Surge","Elastic Hour","Worldline Cut"},
            {"Null Lance","Blank Guard","Delete Future","Quiet Timeline","Blackout Protocol","Anti-Memory","Silence Engine","Void Receipt","No Witness","Unperson","Zero Record","Cut Causality","Static Field","Dead Channel","Clean Room","Cold Storage","Event Horizon","Terminus","Nothing Personal","Final Archive"}
    };

    static {
        buildGenerated();
        addSignatures();
    }

    private static void buildGenerated() {
        for (int s=0; s<SCHOOLS.length; s++) {
            for (int i=0; i<20; i++) {
                String id = SCHOOLS[s].toLowerCase(Locale.US) + "_" + i;
                Card c = makeGenerated(id, NAMES[s][i], SCHOOLS[s], s, i);
                CARDS.put(id, c);
            }
        }
    }

    private static Card makeGenerated(String id, String name, String school, int s, int i) {
        Era era = s==0 ? Era.PAST : s==2 ? Era.FUTURE : s==1 ? Era.PRESENT : (i%3==0?Era.PAST:i%3==1?Era.PRESENT:Era.FUTURE);
        Type type = (i%5==1 || i%5==3) ? Type.SKILL : (i%7==4 ? Type.POWER : Type.ATTACK);
        String rarity = i<8 ? "COMMON" : i<15 ? "UNCOMMON" : "RARE";
        int cost=1, dmg=0, block=0, draw=0, heal=0, paradox=0, shift=0, echo=0, forecast=0, forecastBlock=0;
        int strength=0, dex=0, weak=0, vuln=0, energy=0, pscale=0; boolean exhaust=false, retain=false;

        switch(i) {
            case 0: dmg=7; break;
            case 1: block=7; break;
            case 2: dmg=4; draw=1; break;
            case 3: block=5; draw=1; break;
            case 4: type=Type.POWER; strength=1; paradox=1; exhaust=true; break;
            case 5: dmg=5; echo=5; break;
            case 6: block=6; retain=true; break;
            case 7: dmg=11; cost=2; break;
            case 8: block=8; shift=-1; break;
            case 9: dmg=9; vuln=1; break;
            case 10: block=5; weak=1; break;
            case 11: forecast=14; paradox=-1; cost=2; break;
            case 12: energy=1; draw=1; paradox=1; exhaust=true; cost=0; type=Type.SKILL; break;
            case 13: draw=2; paradox=1; type=Type.SKILL; break;
            case 14: block=12; paradox=-1; cost=2; type=Type.SKILL; break;
            case 15: dmg=6; echo=7; cost=2; break;
            case 16: dmg=4; pscale=2; cost=1; break;
            case 17: dmg=6; paradox=-2; break;
            case 18: heal=5; block=5; exhaust=true; type=Type.SKILL; break;
            case 19: dmg=14; block=8; paradox=2; cost=3; break;
        }

        if (s==0) {
            if (dmg>0 && echo==0 && i%2==0) echo=Math.max(2,dmg/2);
            if (i==13) { draw=3; paradox=0; cost=2; }
            if (i==18) heal=7;
        } else if (s==1) {
            if (dmg>0) dmg+=2;
            if (block>0) block+=2;
            if (i==4) paradox=0;
        } else if (s==2) {
            if (dmg>0 && forecast==0 && i%2==0) { forecast=Math.max(3,dmg/2); dmg=Math.max(2,dmg-2); }
            if (block>0 && i%3==1) { forecastBlock=Math.max(3,block/2); block=Math.max(2,block-2); }
            if (i==12) paradox=0;
        } else if (s==3) {
            if (dmg>0) { dmg+=3; paradox+=1; }
            if (block>0 && i%2==1) { block+=3; paradox+=1; }
            if (i==17) { dmg=9; paradox=-3; }
        } else if (s==4) {
            if (i%4==0) shift = (i%8==0 ? -1 : 1);
            if (i==12) { energy=2; draw=1; paradox=1; }
            if (i==13) { draw=2; paradox=0; }
        } else if (s==5) {
            if (i%4==0) exhaust=true;
            if (i%3==0) paradox-=1;
            if (i==16) { dmg=6; pscale=3; paradox=-1; }
            if (i==18) { block=9; heal=6; paradox=-2; }
        }

        return new Card(id,name,school,era,type,rarity,cost,dmg,block,draw,heal,paradox,shift,echo,forecast,forecastBlock,
                strength,dex,weak,vuln,energy,pscale,exhaust,retain,false);
    }

    private static void addSignatures() {
        add(new Card("sig_first_cause","The First Cause","SIGNATURE",Era.PAST,Type.POWER,"RARE",2,0,0,1,0,-1,0,0,0,0,2,0,0,0,0,0,true,false,false));
        add(new Card("sig_ouroboros","Ouroboros Protocol","SIGNATURE",Era.PAST,Type.ATTACK,"RARE",2,8,0,0,0,1,0,12,0,0,0,0,0,0,0,0,false,false,false));
        add(new Card("sig_now","Absolute Present","SIGNATURE",Era.PRESENT,Type.SKILL,"RARE",2,0,18,2,0,0,0,0,0,0,0,0,0,0,0,0,false,false,false));
        add(new Card("sig_execution","Execution Window","SIGNATURE",Era.PRESENT,Type.ATTACK,"RARE",2,19,0,0,0,0,0,0,0,0,0,0,0,2,0,0,false,false,false));
        add(new Card("sig_last_light","Last Light","SIGNATURE",Era.FUTURE,Type.ATTACK,"RARE",2,0,0,0,0,1,0,0,28,0,0,0,0,0,0,0,false,false,false));
        add(new Card("sig_shelter","Shelter Tomorrow","SIGNATURE",Era.FUTURE,Type.SKILL,"RARE",1,0,3,0,0,0,0,0,0,18,0,0,0,0,0,0,false,false,false));
        add(new Card("sig_impossible","Impossible Victory","SIGNATURE",Era.FUTURE,Type.ATTACK,"RARE",1,5,0,0,0,2,0,0,0,0,0,0,0,0,0,4,true,false,false));
        add(new Card("sig_rewind","Rewind the Rewind","SIGNATURE",Era.PAST,Type.SKILL,"RARE",0,0,4,1,0,2,-1,0,0,0,0,0,0,0,1,0,true,false,false));
        add(new Card("sig_three_body","Three-Body Solution","SIGNATURE",Era.PRESENT,Type.ATTACK,"RARE",3,12,12,1,0,0,0,6,6,0,0,0,0,0,0,0,false,false,false));
        add(new Card("sig_black_star","Black Star Ledger","SIGNATURE",Era.FUTURE,Type.POWER,"RARE",2,0,0,2,0,3,0,0,0,0,3,0,0,0,0,0,true,false,false));
        add(new Card("sig_mercy","Mercy of the Timeline","SIGNATURE",Era.PAST,Type.SKILL,"RARE",2,0,10,0,12,-3,0,0,0,0,0,0,0,0,0,0,true,false,false));
        add(new Card("sig_end","The End Is A Door","SIGNATURE",Era.FUTURE,Type.ATTACK,"RARE",3,24,0,0,0,-2,1,0,12,0,0,0,0,0,0,0,true,false,false));
    }

    private static void add(Card c) { CARDS.put(c.id,c); }

    public static Card get(String key) {
        if (key == null) return null;
        boolean up = key.endsWith("#U");
        String id = up ? key.substring(0,key.length()-2) : key;
        Card c = CARDS.get(id);
        return c == null ? null : (up ? c.upgrade() : c);
    }

    public static List<Card> all() { return new ArrayList<>(CARDS.values()); }

    public static List<Card> rewardPool(Random r, int count) {
        ArrayList<Card> all = new ArrayList<>(CARDS.values());
        ArrayList<Card> out = new ArrayList<>();
        int guard=0;
        while (out.size()<count && guard++<500) {
            Card c=all.get(r.nextInt(all.size()));
            int roll=r.nextInt(100);
            boolean ok = c.rarity.equals("COMMON") ? roll<58 : c.rarity.equals("UNCOMMON") ? roll<82 : roll<35;
            if (ok && !out.contains(c)) out.add(c);
        }
        while (out.size()<count) out.add(all.get(r.nextInt(all.size())));
        return out;
    }

    public static int size() { return CARDS.size(); }
}
