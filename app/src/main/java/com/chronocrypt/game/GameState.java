package com.chronocrypt.game;

import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

public final class GameState {
    public enum Screen { MENU, HELP, MAP, COMBAT, REWARD, EVENT, SHOP, REST, CODEX, DECK, STATS, GAME_OVER, VICTORY }
    public enum NodeType { BATTLE, ELITE, EVENT, SHOP, REST, BOSS }

    public final SharedPreferences prefs;
    public final Random rng = new Random();
    public Screen screen = Screen.MENU;
    public Screen returnScreen = Screen.MENU;
    public int seed, act=1, floor=0, hp=80, maxHp=80, gold=75;
    public ArrayList<String> deck = new ArrayList<>();
    public ArrayList<String> relics = new ArrayList<>();
    public NodeType[] choices = new NodeType[3];
    public Combat combat;
    public NodeType currentNode;
    public ArrayList<Card> rewardCards = new ArrayList<>();
    public Relic rewardRelic;
    public ArrayList<Card> shopCards = new ArrayList<>();
    public ArrayList<Relic> shopRelics = new ArrayList<>();
    public int eventId;
    public String eventTitle="", eventBody="";
    public String[] eventChoices = new String[2];
    public ArrayList<Integer> upgradeCandidates = new ArrayList<>();
    public int codexPage=0;
    public boolean activeRun=false;

    public GameState(SharedPreferences prefs) {
        this.prefs=prefs;
        activeRun=prefs.getBoolean("run_active",false);
    }

    public boolean hasRelic(String id){return relics.contains(id);}

    public void newRun(){
        seed=(int)(System.currentTimeMillis() ^ (System.nanoTime()>>>17));
        rng.setSeed(seed);
        act=1; floor=0; maxHp=80; hp=80; gold=75; activeRun=true;
        deck.clear(); relics.clear();
        for(int i=0;i<4;i++) deck.add("vanguard_0");
        for(int i=0;i<4;i++) deck.add("vanguard_1");
        deck.add("weaver_10");
        deck.add("oracle_0");
        relics.add("chronometer");
        generateChoices();
        screen=Screen.MAP;
        incrementMeta("runs",1);
        saveRun();
    }

    public void abandonRun(){
        activeRun=false; combat=null; prefs.edit().putBoolean("run_active",false).apply(); screen=Screen.MENU;
    }

    public boolean loadRun(){
        if(!prefs.getBoolean("run_active",false)) return false;
        try{
            seed=prefs.getInt("seed",1); rng.setSeed(seed + prefs.getInt("rng_salt",0));
            act=prefs.getInt("act",1); floor=prefs.getInt("floor",0); hp=prefs.getInt("hp",80);
            maxHp=prefs.getInt("max_hp",80); gold=prefs.getInt("gold",75);
            deck.clear(); relics.clear();
            JSONArray d=new JSONArray(prefs.getString("deck","[]"));
            for(int i=0;i<d.length();i++) if(CardLibrary.get(d.getString(i))!=null) deck.add(d.getString(i));
            JSONArray r=new JSONArray(prefs.getString("relics","[]"));
            for(int i=0;i<r.length();i++) if(RelicLibrary.get(r.getString(i))!=null) relics.add(r.getString(i));
            activeRun=true; generateChoices(); screen=Screen.MAP; return true;
        }catch(Exception e){
            activeRun=false; prefs.edit().putBoolean("run_active",false).apply(); return false;
        }
    }

    public void saveRun(){
        if(!activeRun) return;
        JSONArray d=new JSONArray(); for(String x:deck)d.put(x);
        JSONArray r=new JSONArray(); for(String x:relics)r.put(x);
        prefs.edit().putBoolean("run_active",true).putInt("seed",seed).putInt("rng_salt",rng.nextInt())
                .putInt("act",act).putInt("floor",floor).putInt("hp",hp).putInt("max_hp",maxHp).putInt("gold",gold)
                .putString("deck",d.toString()).putString("relics",r.toString()).apply();
    }

    private void incrementMeta(String key,int delta){prefs.edit().putInt(key,prefs.getInt(key,0)+delta).apply();}
    public int stat(String key){return prefs.getInt(key,0);}

    public void generateChoices(){
        if(floor>=11){choices[0]=choices[1]=choices[2]=NodeType.BOSS;return;}
        for(int i=0;i<3;i++) choices[i]=rollNode(i);
        if(floor==0){choices[0]=NodeType.BATTLE; choices[1]=NodeType.BATTLE;}
    }

    private NodeType rollNode(int slot){
        int x=rng.nextInt(100);
        boolean rich=hasRelic("retrocausal_map");
        if(x<43) return NodeType.BATTLE;
        if(x<56) return NodeType.ELITE;
        if(x<(rich?74:70)) return NodeType.EVENT;
        if(x<(rich?86:81)) return NodeType.SHOP;
        return NodeType.REST;
    }

    public void chooseNode(NodeType type){
        currentNode=type; floor++;
        if(floor>prefs.getInt("best_floor",0)) prefs.edit().putInt("best_floor",(act-1)*12+floor).apply();
        switch(type){
            case BATTLE: startCombat(false,false); break;
            case ELITE: startCombat(true,false); break;
            case BOSS: startCombat(false,true); break;
            case EVENT: prepareEvent(); screen=Screen.EVENT; break;
            case SHOP: prepareShop(); screen=Screen.SHOP; break;
            case REST: prepareRest(); screen=Screen.REST; break;
        }
        saveRun();
    }

    private void startCombat(boolean elite, boolean boss){
        Enemy e=boss?EnemyLibrary.boss(act):EnemyLibrary.random(rng,act,elite);
        combat=new Combat(this,e);
        screen=Screen.COMBAT;
    }

    public void onCombatWon(){
        int base = currentNode==NodeType.ELITE?48:currentNode==NodeType.BOSS?90:28;
        if(hasRelic("compound_interest")) base=(int)Math.ceil(base*1.2);
        gold += base + rng.nextInt(13);
        if(hasRelic("mending_loop")) hp=Math.min(maxHp,hp+5);
        rewardCards.clear(); rewardCards.addAll(CardLibrary.rewardPool(rng,3));
        rewardRelic = currentNode==NodeType.ELITE || currentNode==NodeType.BOSS ? RelicLibrary.random(rng,relics) : null;
        screen=Screen.REWARD;
    }

    public void takeRewardCard(Card c){if(c!=null)deck.add(c.id); finishReward();}
    public void skipReward(){finishReward();}
    private void finishReward(){
        if(rewardRelic!=null){acquireRelic(rewardRelic); rewardRelic=null;}
        if(currentNode==NodeType.BOSS){
            if(act>=3){victory();return;}
            act++; floor=0; hp=Math.min(maxHp,hp+(int)(maxHp*.35f));
        }
        completeNode();
    }

    public void acquireRelic(Relic relic){
        if(relic==null||relics.contains(relic.id))return;
        relics.add(relic.id);
        if(relic.id.equals("second_heart")){maxHp+=12;hp+=12;}
        if(relic.id.equals("old_coin"))gold+=120;
    }

    public void completeNode(){combat=null; generateChoices(); screen=Screen.MAP; saveRun();}

    public void gameOver(){
        activeRun=false; incrementMeta("deaths",1); prefs.edit().putBoolean("run_active",false).apply(); screen=Screen.GAME_OVER;
    }
    public void victory(){
        activeRun=false; incrementMeta("wins",1); incrementMeta("shards",25+act*5); prefs.edit().putBoolean("run_active",false).apply(); screen=Screen.VICTORY;
    }

    public void prepareShop(){
        shopCards.clear(); shopCards.addAll(CardLibrary.rewardPool(rng,5));
        shopRelics.clear(); for(int i=0;i<2;i++){Relic r=RelicLibrary.random(rng,relics);if(r!=null&&!shopRelics.contains(r))shopRelics.add(r);}    
    }
    public int cardPrice(Card c){return c.rarity.equals("RARE")?125:c.rarity.equals("UNCOMMON")?82:48;}
    public int relicPrice(Relic r){return r.rarity.equals("RARE")?190:r.rarity.equals("UNCOMMON")?145:105;}
    public boolean buyCard(Card c){int p=cardPrice(c);if(gold<p)return false;gold-=p;deck.add(c.id);shopCards.remove(c);saveRun();return true;}
    public boolean buyRelic(Relic r){int p=relicPrice(r);if(gold<p)return false;gold-=p;acquireRelic(r);shopRelics.remove(r);saveRun();return true;}
    public boolean removeCard(){
        if(gold<65||deck.size()<=5)return false;
        int idx=-1;for(int i=0;i<deck.size();i++){String k=deck.get(i);if(k.startsWith("vanguard_0")||k.startsWith("vanguard_1")){idx=i;break;}}
        if(idx<0)idx=0; deck.remove(idx);gold-=65;saveRun();return true;
    }

    public void prepareRest(){
        upgradeCandidates.clear();
        ArrayList<Integer> valid=new ArrayList<>();
        for(int i=0;i<deck.size();i++)if(!deck.get(i).endsWith("#U"))valid.add(i);
        Collections.shuffle(valid,rng); for(int i=0;i<Math.min(3,valid.size());i++)upgradeCandidates.add(valid.get(i));
    }
    public void restHeal(){hp=Math.min(maxHp,hp+(int)Math.ceil(maxHp*.30));completeNode();}
    public void restUpgrade(int deckIndex){
        if(deckIndex>=0&&deckIndex<deck.size()&&!deck.get(deckIndex).endsWith("#U"))deck.set(deckIndex,deck.get(deckIndex)+"#U");
        completeNode();
    }

    public void prepareEvent(){
        eventId=rng.nextInt(12);
        switch(eventId){
            case 0: eventTitle="THE MESSAGE YOU SENT"; eventBody="A distress packet carries your own encryption key and a timestamp twelve years from now."; eventChoices=new String[]{"Open it • gain a Rare card, lose 9 HP","Erase it • gain 70 Credits"};break;
            case 1: eventTitle="DINNER WITH YESTERDAY"; eventBody="You meet an earlier version of yourself who has not yet learned to fear the Chronocrypt."; eventChoices=new String[]{"Teach them • upgrade a random card","Lie to them • heal 18 HP"};break;
            case 2: eventTitle="THE DEAD MARKET"; eventBody="Merchants sell artifacts from civilizations whose timelines have already collapsed."; eventChoices=new String[]{"Buy a sealed relic • 90 Credits","Sell a memory • +14 max HP, lose 12 current HP"};break;
            case 3: eventTitle="BOOTSTRAP LIBRARY"; eventBody="Every book cites another book in the room. None has an author."; eventChoices=new String[]{"Study recursion • add 2 Archive cards","Burn the index • remove a basic card"};break;
            case 4: eventTitle="TWO DOORS, SAME ROOM"; eventBody="One door is labeled PAST. One is labeled FUTURE. Both open behind you."; eventChoices=new String[]{"Past • heal 25, lose 45 Credits","Future • gain 110 Credits, lose 12 HP"};break;
            case 5: eventTitle="UNBORN WARSHIP"; eventBody="A starship blueprint begs you not to build the vessel that sent it backward."; eventChoices=new String[]{"Honor the warning • gain Null card + 35 Credits","Build the gun • gain a Rare attack, lose 10 HP"};break;
            case 6: eventTitle="CAUSAL STORM"; eventBody="Minutes fracture into razor-thin duplicates around the hull."; eventChoices=new String[]{"Ride it • gain Weaver relic, lose 8 HP","Anchor down • heal 12 HP"};break;
            case 7: eventTitle="THE LAST MUSEUM"; eventBody="A curator preserves objects from futures that no longer exist."; eventChoices=new String[]{"Donate a card • gain 120 Credits","Steal an exhibit • gain random relic, lose 15 HP"};break;
            case 8: eventTitle="PARADOX ORCHARD"; eventBody="Fruit ripens before blossoms open. The seeds remember being eaten."; eventChoices=new String[]{"Eat • fully heal, reduce max HP by 8","Take seeds • add 2 Entropy cards"};break;
            case 9: eventTitle="AN EMPTY COFFIN"; eventBody="Your name is etched into a coffin manufactured centuries ago."; eventChoices=new String[]{"Open it • gain Dead God's Watch","Walk away • gain 50 Credits"};break;
            case 10:eventTitle="FORECAST EXCHANGE";eventBody="An oracle offers to trade certainty for possibility.";eventChoices=new String[]{"Buy certainty • 75 Credits, gain Oracle Eye","Refuse certainty • add 2 Future cards"};break;
            default:eventTitle="THE MISSING SECOND";eventBody="The universe has misplaced exactly one second. You can feel it waiting inside your chest.";eventChoices=new String[]{"Keep it • +1 random upgraded card","Return it • heal 20 and gain 40 Credits"};
        }
    }

    public void resolveEvent(int choice){
        switch(eventId){
            case 0: if(choice==0){hp-=9;addRandomCard("RARE",null);}else gold+=70;break;
            case 1: if(choice==0)upgradeRandom();else hp=Math.min(maxHp,hp+18);break;
            case 2: if(choice==0&&gold>=90){gold-=90;acquireRelic(RelicLibrary.random(rng,relics));}else if(choice==1){maxHp+=14;hp=Math.max(1,hp-12);}break;
            case 3: if(choice==0){addRandomCard(null,"ARCHIVE");addRandomCard(null,"ARCHIVE");}else removeFreeBasic();break;
            case 4: if(choice==0&&gold>=45){gold-=45;hp=Math.min(maxHp,hp+25);}else if(choice==1){gold+=110;hp-=12;}break;
            case 5: if(choice==0){addRandomCard(null,"NULL");gold+=35;}else{addRandomCard("RARE",null);hp-=10;}break;
            case 6: if(choice==0){Relic r=RelicLibrary.get("redshift_drive");acquireRelic(r);hp-=8;}else hp=Math.min(maxHp,hp+12);break;
            case 7: if(choice==0){if(deck.size()>5){deck.remove(rng.nextInt(deck.size()));gold+=120;}}else{acquireRelic(RelicLibrary.random(rng,relics));hp-=15;}break;
            case 8: if(choice==0){maxHp=Math.max(45,maxHp-8);hp=maxHp;}else{addRandomCard(null,"ENTROPY");addRandomCard(null,"ENTROPY");}break;
            case 9: if(choice==0)acquireRelic(RelicLibrary.get("dead_god_watch"));else gold+=50;break;
            case 10:if(choice==0&&gold>=75){gold-=75;acquireRelic(RelicLibrary.get("oracle_eye"));}else{addRandomCard(null,"ORACLE");addRandomCard(null,"ORACLE");}break;
            default:if(choice==0){addRandomCard(null,null);upgradeRandom();}else{hp=Math.min(maxHp,hp+20);gold+=40;}break;
        }
        if(hp<=0){gameOver();return;} completeNode();
    }

    private void addRandomCard(String rarity,String school){
        ArrayList<Card> pool=new ArrayList<>();
        for(Card c:CardLibrary.all())if((rarity==null||c.rarity.equals(rarity))&&(school==null||c.school.equals(school)))pool.add(c);
        if(!pool.isEmpty())deck.add(pool.get(rng.nextInt(pool.size())).id);
    }
    private void upgradeRandom(){ArrayList<Integer> v=new ArrayList<>();for(int i=0;i<deck.size();i++)if(!deck.get(i).endsWith("#U"))v.add(i);if(!v.isEmpty()){int i=v.get(rng.nextInt(v.size()));deck.set(i,deck.get(i)+"#U");}}
    private void removeFreeBasic(){for(int i=0;i<deck.size();i++)if(deck.get(i).startsWith("vanguard_0")||deck.get(i).startsWith("vanguard_1")){deck.remove(i);return;}}

    public static final class TemporalEffect {
        int turns, damage, block; String label;
        TemporalEffect(int t,int d,int b,String l){turns=t;damage=d;block=b;label=l;}
        TemporalEffect copy(){return new TemporalEffect(turns,damage,block,label);}
    }

    public static final class CombatSnapshot {
        int hp, block, energy, chrono, era, paradox, force, phase, enemyHp, enemyStrength, enemyWeak, enemyVuln;
        ArrayList<String> draw=new ArrayList<>(),hand=new ArrayList<>(),discard=new ArrayList<>(),exhaust=new ArrayList<>();
        ArrayList<TemporalEffect> effects=new ArrayList<>();
    }

    public static final class Combat {
        final GameState g;
        public final Enemy enemy;
        public ArrayList<Card> draw=new ArrayList<>(), hand=new ArrayList<>(), discard=new ArrayList<>(), exhaust=new ArrayList<>();
        public ArrayList<TemporalEffect> effects=new ArrayList<>();
        public int energy, chrono, block, era=1, paradox=0, force=0, phase=0, turn=0;
        public int selected=-1;
        public boolean rewound=false, shifted=false, oracleDrawUsed=false, deathSaveUsed=false;
        public String log="Timeline synchronized.";
        CombatSnapshot snapshot;

        Combat(GameState g,Enemy e){
            this.g=g;this.enemy=e;
            for(String k:g.deck){Card c=CardLibrary.get(k);if(c!=null)draw.add(c);} Collections.shuffle(draw,g.rng);
            block=g.hasRelic("causal_armor")?8:0;
            startTurn();
        }

        private Card drawOne(){if(draw.isEmpty()){draw.addAll(discard);discard.clear();Collections.shuffle(draw,g.rng);}if(draw.isEmpty())return null;Card c=draw.remove(draw.size()-1);hand.add(c);return c;}
        private void drawCards(int n){for(int i=0;i<n&&hand.size()<10;i++)drawOne();}

        public void startTurn(){
            turn++; enemy.turn=turn-1; energy=3+(g.hasRelic("epoch_crown")?1:0)+(turn==1&&g.hasRelic("glass_battery")?1:0)+(turn%3==0&&g.hasRelic("combat_clock")?1:0);
            chrono=1+(g.hasRelic("chronometer")?1:0); block= turn==1&&g.hasRelic("causal_armor")?8:0; rewound=false; shifted=false; oracleDrawUsed=false;
            if(g.hasRelic("entropy_sink")&&paradox>=8)paradox--;
            resolveTemporal();
            int target=5+(turn==1&&g.hasRelic("black_archive")?1:0); drawCards(Math.max(0,target-hand.size()));
            if(paradox>=9){damagePlayer(3);log="Paradox burn deals 3 temporal damage.";}
            snapshot=capture();
        }

        private void resolveTemporal(){
            Iterator<TemporalEffect> it=effects.iterator();
            while(it.hasNext()){
                TemporalEffect x=it.next();x.turns--;
                if(x.turns<=0){
                    if(x.damage>0){int d=x.damage+(g.hasRelic("tomorrow_coil")&&x.label.contains("Forecast")?4:0)+(g.hasRelic("mirror_shard")&&x.label.contains("Echo")?4:0);dealDamage(d);}
                    if(x.block>0)block+=x.block+(g.hasRelic("tomorrow_coil")&&x.label.contains("Forecast")?2:0);
                    log=x.label+" resolves.";it.remove();
                }
            }
        }

        public boolean play(int index){
            if(index<0||index>=hand.size())return false; Card c=hand.get(index); if(energy<c.cost)return false;
            energy-=c.cost;
            boolean sync=c.era.ordinal()==era;
            if(!sync)paradox=Math.min(12,paradox+1);
            int syncBonus=sync?2:0;
            if(sync&&g.hasRelic("trinary_core"))syncBonus++;
            if(sync&&c.era==Card.Era.PAST&&g.hasRelic("archive_key"))syncBonus+=2;
            int d=c.damage+force+(c.paradoxScale*(paradox));
            if(c.paradoxScale>0&&g.hasRelic("event_horizon"))d+=paradox;
            if(paradox>=5&&g.hasRelic("paradox_lens")&&d>0)d+=3;
            if(sync&&d>0)d+=syncBonus;
            if(d>0)dealDamage(d);
            int b=c.block+phase;
            if(sync&&b>0)b+=syncBonus+(g.hasRelic("phase_prism")?3:0);
            if(b>0)block+=b;
            if(c.draw>0)drawCards(c.draw);
            if(c.heal>0)g.hp=Math.min(g.maxHp,g.hp+c.heal);
            if(c.energy>0)energy+=c.energy;
            if(c.strength>0)force+=c.strength;
            if(c.dexterity>0)phase+=c.dexterity;
            if(c.weak>0)enemy.weak+=c.weak;
            if(c.vulnerable>0)enemy.vulnerable+=c.vulnerable;
            paradox=Math.max(0,Math.min(12,paradox+c.paradox));
            if(c.shift!=0)era=Math.max(0,Math.min(2,era+c.shift));
            if(c.echoDamage>0)effects.add(new TemporalEffect(1,c.echoDamage,0,"Echo"));
            if(c.forecastDamage>0||c.forecastBlock>0)effects.add(new TemporalEffect(2,c.forecastDamage,c.forecastBlock,"Forecast"));
            if(sync&&c.era==Card.Era.FUTURE&&g.hasRelic("oracle_eye")&&!oracleDrawUsed){drawCards(1);oracleDrawUsed=true;}
            Card played=hand.remove(index);
            if(c.exhaust||c.type==Card.Type.POWER)exhaust.add(played); else discard.add(played);
            selected=-1;
            log=(sync?"SYNC • ":"CROSS-ERA • ")+c.name;
            if(enemy.hp<=0){g.onCombatWon();return true;}
            return true;
        }

        private void dealDamage(int raw){
            int d=raw; if(enemy.vulnerable>0)d=(int)Math.ceil(d*1.5f); enemy.hp=Math.max(0,enemy.hp-Math.max(0,d));
        }

        public boolean shift(int dir){
            int to=Math.max(0,Math.min(2,era+dir));if(to==era)return false;
            boolean free=g.hasRelic("redshift_drive")&&!shifted;
            if(!free){if(chrono<=0)return false;chrono--;}
            era=to;shifted=true;log="Worldline shifted to "+eraName()+".";return true;
        }

        public String eraName(){return era==0?"PAST":era==1?"PRESENT":"FUTURE";}

        public boolean rewind(){
            if(rewound||snapshot==null)return false;
            int cost=g.hasRelic("anchor_spike")?1:2;
            restore(snapshot); paradox=Math.min(12,paradox+cost); rewound=true; log="REWIND • actions undone, causality strained.";return true;
        }

        public void endTurn(){
            if(g.screen!=Screen.COMBAT)return;
            ArrayList<Card> kept=new ArrayList<>();
            for(Card c:hand){if(c.retain)kept.add(c);else discard.add(c);}hand.clear();hand.addAll(kept);
            enemyAction();
            if(g.hp<=0){g.gameOver();return;}
            if(enemy.weak>0)enemy.weak--;if(enemy.vulnerable>0)enemy.vulnerable--;
            startTurn();
        }

        private void enemyAction(){
            int d=enemy.intentDamage(paradox)+(g.hasRelic("epoch_crown")?2:0);
            if(enemy.pattern==4&&enemy.turn%3==0){enemy.strength+=3;log=enemy.name+" calibrates: +3 Force.";}
            else{
                int absorbed=Math.min(block,d);block-=absorbed;int hpD=d-absorbed;damagePlayer(hpD);log=enemy.name+" attacks for "+d+".";
                if(enemy.pattern==2&&enemy.turn%2==1)paradox=Math.min(12,paradox+1);
                if(enemy.pattern==3&&enemy.turn%4==3)enemy.strength+=1;
            }
            enemy.turn++;
        }

        private void damagePlayer(int d){
            if(d<=0)return;g.hp-=d;
            if(g.hp<=0&&g.hasRelic("dead_god_watch")&&!deathSaveUsed){g.hp=1;deathSaveUsed=true;log="Dead God's Watch rejects this death.";}
        }

        private CombatSnapshot capture(){
            CombatSnapshot s=new CombatSnapshot();s.hp=g.hp;s.block=block;s.energy=energy;s.chrono=chrono;s.era=era;s.paradox=paradox;s.force=force;s.phase=phase;
            s.enemyHp=enemy.hp;s.enemyStrength=enemy.strength;s.enemyWeak=enemy.weak;s.enemyVuln=enemy.vulnerable;
            for(Card c:draw)s.draw.add(c.key());for(Card c:hand)s.hand.add(c.key());for(Card c:discard)s.discard.add(c.key());for(Card c:exhaust)s.exhaust.add(c.key());
            for(TemporalEffect x:effects)s.effects.add(x.copy());return s;
        }
        private void restore(CombatSnapshot s){
            g.hp=s.hp;block=s.block;energy=s.energy;chrono=s.chrono;era=s.era;paradox=s.paradox;force=s.force;phase=s.phase;
            enemy.hp=s.enemyHp;enemy.strength=s.enemyStrength;enemy.weak=s.enemyWeak;enemy.vulnerable=s.enemyVuln;
            draw=cards(s.draw);hand=cards(s.hand);discard=cards(s.discard);exhaust=cards(s.exhaust);effects.clear();for(TemporalEffect x:s.effects)effects.add(x.copy());selected=-1;
        }
        private ArrayList<Card> cards(ArrayList<String> keys){ArrayList<Card> a=new ArrayList<>();for(String k:keys){Card c=CardLibrary.get(k);if(c!=null)a.add(c);}return a;}
    }
}
