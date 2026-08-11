package com.chronocrypt.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import java.util.*;

public final class ChronoGameView extends View {
    private static final float VW=1080f,VH=2400f;
    private static final int BG=Color.rgb(5,8,18), PANEL=Color.rgb(14,22,38), PANEL2=Color.rgb(22,32,52);
    private static final int TEXT=Color.rgb(232,239,248), MUTED=Color.rgb(133,154,180), CYAN=Color.rgb(63,219,255), GOLD=Color.rgb(255,200,94), MAGENTA=Color.rgb(233,88,255), RED=Color.rgb(255,84,105), GREEN=Color.rgb(91,231,158);
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<Hotspot> hits=new ArrayList<>();
    private final float[] starX=new float[88],starY=new float[88],starR=new float[88];
    private final GameState g;
    private final SoundManager sound;
    private float scale=1,offX=0,offY=0;
    private int helpPage=0, deckPage=0, relicPage=0;
    private long start=System.nanoTime();
    private String toast=""; private long toastUntil=0;

    private static final class Hotspot { RectF r; Runnable a; Hotspot(RectF r,Runnable a){this.r=r;this.a=a;} }

    public ChronoGameView(Context c){
        super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setBackground(new ColorDrawable(BG));
        SharedPreferences prefs=c.getSharedPreferences("chronocrypt_v1",Context.MODE_PRIVATE);
        g=new GameState(prefs);sound=new SoundManager(c);
        Random r=new Random(4902187);for(int i=0;i<starX.length;i++){starX[i]=r.nextFloat()*VW;starY[i]=r.nextFloat()*VH;starR[i]=.6f+r.nextFloat()*2.2f;}
        stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(2);
    }

    public void persist(){g.saveRun();}
    public void destroy(){sound.release();}
    public boolean handleBack(){
        if(g.screen==GameState.Screen.MENU)return false;
        if(g.screen==GameState.Screen.COMBAT){toast("Finish the encounter or rewind the timeline.");return true;}
        if(g.screen==GameState.Screen.DECK||g.screen==GameState.Screen.CODEX||g.screen==GameState.Screen.STATS||g.screen==GameState.Screen.HELP){g.screen=g.returnScreen;invalidate();return true;}
        if(g.screen==GameState.Screen.MAP){g.saveRun();g.screen=GameState.Screen.MENU;invalidate();return true;}
        return true;
    }

    @Override protected void onDraw(Canvas real){
        super.onDraw(real);hits.clear();float s=Math.min(getWidth()/VW,getHeight()/VH);scale=s;offX=(getWidth()-VW*s)/2f;offY=(getHeight()-VH*s)/2f;
        real.save();real.translate(offX,offY);real.scale(s,s);
        drawBackground(real);
        switch(g.screen){
            case MENU:drawMenu(real);break; case HELP:drawHelp(real);break; case MAP:drawMap(real);break; case COMBAT:drawCombat(real);break;
            case REWARD:drawReward(real);break; case EVENT:drawEvent(real);break; case SHOP:drawShop(real);break; case REST:drawRest(real);break;
            case CODEX:drawCodex(real);break;case DECK:drawDeck(real);break;case STATS:drawStats(real);break;case GAME_OVER:drawGameOver(real,false);break;case VICTORY:drawGameOver(real,true);break;
        }
        if(System.currentTimeMillis()<toastUntil)drawToast(real);
        real.restore();postInvalidateDelayed(33);
    }

    private void drawBackground(Canvas c){
        c.drawColor(BG);float t=(System.nanoTime()-start)/1_000_000_000f;
        p.setStyle(Paint.Style.FILL);
        for(int i=0;i<starX.length;i++){int a=55+(int)(45*Math.sin(t*.7+i*.83));p.setColor(Color.argb(Math.max(20,a),120,185,225));c.drawCircle(starX[i],starY[i],starR[i],p);}
        p.setShader(new LinearGradient(0,0,0,VH,Color.argb(75,20,47,79),Color.argb(0,5,8,18),Shader.TileMode.CLAMP));c.drawRect(0,0,VW,VH,p);p.setShader(null);
        stroke.setColor(Color.argb(25,80,185,220));stroke.setStrokeWidth(1);for(int y=180;y<VH;y+=160)c.drawLine(0,y,VW,y,stroke);
    }

    private void drawMenu(Canvas c){
        smallCaps(c,"A ROGUELIKE DECKBUILDER",VW/2,250,CYAN,34,true);
        title(c,"CHRONO",VW/2,430,112,TEXT,true);title(c,"CRYPT",VW/2,535,112,GOLD,true);
        stroke.setStrokeWidth(5);stroke.setColor(CYAN);c.drawCircle(VW/2,780,172,stroke);stroke.setColor(MAGENTA);c.drawCircle(VW/2,780,126,stroke);
        c.drawLine(VW/2-120,650,VW/2+120,910,stroke);c.drawLine(VW/2+120,650,VW/2-120,910,stroke);
        smallCaps(c,"BREAK HISTORY. BUILD THE DECK THAT SURVIVES IT.",VW/2,1035,MUTED,25,true);
        if(g.prefs.getBoolean("run_active",false)) button(c,175,1165,730,125,"CONTINUE RUN",CYAN,()->{sound.click();g.loadRun();});
        button(c,175,1315,730,125,"NEW TIMELINE",GOLD,()->{sound.reward();g.newRun();if(!g.prefs.getBoolean("tutorial_seen",false)){g.returnScreen=GameState.Screen.MAP;g.screen=GameState.Screen.HELP;helpPage=0;}});
        button(c,175,1465,355,112,"CARD CODEX",TEXT,()->{sound.click();g.returnScreen=GameState.Screen.MENU;g.screen=GameState.Screen.CODEX;g.codexPage=0;});
        button(c,550,1465,355,112,"HOW TO PLAY",TEXT,()->{sound.click();g.returnScreen=GameState.Screen.MENU;g.screen=GameState.Screen.HELP;helpPage=0;});
        button(c,175,1602,355,112,"CHRONICLE",TEXT,()->{sound.click();g.returnScreen=GameState.Screen.MENU;g.screen=GameState.Screen.STATS;});
        button(c,550,1602,355,112,sound.isEnabled()?"SOUND: ON":"SOUND: OFF",TEXT,()->{sound.setEnabled(!sound.isEnabled());sound.click();invalidate();});
        panel(c,145,1810,790,250,Color.argb(160,13,21,36),Color.argb(100,63,219,255));
        text(c,"V1.0 • OFFLINE • 132 CARDS • 24 RELICS",VW/2,1885,24,MUTED,true);
        text(c,"Three eras. Three acts. One causality budget.",VW/2,1940,31,TEXT,true);
        text(c,"Your choices survive even when the timeline does not.",VW/2,1990,25,MUTED,true);
    }

    private void drawHelp(Canvas c){
        header(c,"FIELD MANUAL","PAGE "+(helpPage+1)+" / 4",()->{g.screen=g.returnScreen;});
        String h="",body="";String[] bullets;
        if(helpPage==0){h="THE RUN";body="Descend through three fractured eras of the Chronocrypt. Each floor offers three routes: combat, elite anomalies, events, shops, repair stations, and act bosses.";bullets=new String[]{"Build a permanent deck during the run.","Credits buy cards, relics, and deck thinning.","Death ends the timeline; Chronicle statistics persist."};}
        else if(helpPage==1){h="ERA SYNCHRONY";body="Every card belongs to PAST, PRESENT, or FUTURE. Your ship also occupies one era. Matching a card to the current era gives a Synchrony bonus. Playing cross-era is legal, but creates Paradox.";bullets=new String[]{"PAST specializes in Echoes and recursion.","PRESENT is immediate, efficient, and defensive.","FUTURE creates Forecasts that resolve later.","Chrono lets you shift eras during combat."};}
        else if(helpPage==2){h="PARADOX & REWIND";body="At the start of each turn, the game records a causality snapshot. REWIND restores that snapshot once per turn, undoing your actions—but the memory of the rewind remains as Paradox.";bullets=new String[]{"5+ Paradox empowers several dangerous builds.","6+ Paradox also empowers enemy attacks.","9+ Paradox burns you at turn start.","Some cards vent Paradox; others weaponize it."};}
        else{h="READ THE ENEMY";body="Enemy intent is deterministic for the current turn. Block is lost at the start of your next turn. Force increases attacks; Phase increases block. Weak reduces outgoing damage; Exposed increases incoming damage.";bullets=new String[]{"Forecasts resolve after two turn starts.","Echoes repeat damage next turn.","Powers exhaust after granting permanent combat buffs.","Bosses punish passive decks; build a real engine."};}
        smallCaps(c,h,90,420,GOLD,44,false);wrap(c,body,90,505,900,38,TEXT,1.35f);
        float y=850;for(String b:bullets){p.setColor(CYAN);c.drawCircle(112,y-10,8,p);wrap(c,b,145,y-34,820,31,MUTED,1.25f);y+=155;}
        if(helpPage>0)button(c,85,2110,360,110,"PREVIOUS",TEXT,()->{sound.click();helpPage--;});
        if(helpPage<3)button(c,635,2110,360,110,"NEXT",CYAN,()->{sound.click();helpPage++;});
        else button(c,635,2110,360,110,"ENTER TIMELINE",GOLD,()->{sound.reward();g.prefs.edit().putBoolean("tutorial_seen",true).apply();g.screen=g.returnScreen;});
    }

    private void drawMap(Canvas c){
        runHeader(c,"TIMELINE MAP");
        String zone=g.act==1?"THE PALE ARCHIVE":g.act==2?"THE REDSHIFT CITADEL":"THE LAST MACHINE";
        smallCaps(c,"ACT "+g.act+" • "+zone,VW/2,330,GOLD,28,true);text(c,"FLOOR "+g.floor+" / 12",VW/2,382,30,MUTED,true);
        stroke.setColor(Color.argb(100,63,219,255));stroke.setStrokeWidth(4);c.drawLine(VW/2,500,VW/2,1750,stroke);
        for(int i=0;i<3;i++){
            final int idx=i;float x=90+i*335;float y=760+(i==1?-100:60);GameState.NodeType n=g.choices[i];int col=nodeColor(n);
            panel(c,x,y,285,520,Color.argb(225,15,24,42),Color.argb(210,Color.red(col),Color.green(col),Color.blue(col)));
            text(c,nodeGlyph(n),x+142,y+135,90,col,true);smallCaps(c,nodeName(n),x+142,y+260,col,27,true);wrap(c,nodeHint(n),x+30,y+315,225,24,MUTED,1.2f);
            hits.add(new Hotspot(new RectF(x,y,x+285,y+520),()->{sound.click();g.chooseNode(g.choices[idx]);}));
        }
        text(c,"Choose one branch. The other histories collapse.",VW/2,1465,27,MUTED,true);
        button(c,90,1705,285,105,"DECK • "+g.deck.size(),TEXT,()->{sound.click();g.returnScreen=GameState.Screen.MAP;deckPage=0;g.screen=GameState.Screen.DECK;});
        button(c,397,1705,285,105,"RELICS • "+g.relics.size(),TEXT,()->{sound.click();g.returnScreen=GameState.Screen.MAP;relicPage=0;g.screen=GameState.Screen.STATS;});
        button(c,705,1705,285,105,"SAVE & EXIT",TEXT,()->{sound.click();g.saveRun();g.screen=GameState.Screen.MENU;});
        panel(c,90,1885,900,250,Color.argb(140,11,19,33),Color.argb(60,255,200,94));
        smallCaps(c,"RUN STATUS",125,1950,GOLD,23,false);text(c,"Hull",125,2010,24,MUTED,false);bar(c,230,1986,430,30,g.hp/(float)g.maxHp,GREEN);text(c,g.hp+" / "+g.maxHp,680,2012,24,TEXT,false);
        text(c,"Credits  "+g.gold,125,2075,27,TEXT,false);text(c,"Seed  "+Integer.toUnsignedString(g.seed),510,2075,22,MUTED,false);
    }

    private void drawCombat(Canvas c){
        GameState.Combat b=g.combat;if(b==null){g.completeNode();return;}
        runHeader(c,"ENCOUNTER • TURN "+b.turn);
        panel(c,80,300,920,640,Color.argb(190,12,20,36),Color.argb(115,255,84,105));
        smallCaps(c,b.enemy.boss?"BOSS ANOMALY":b.enemy.elite?"ELITE ANOMALY":"HOSTILE ANOMALY",120,365,b.enemy.boss?GOLD:RED,24,false);
        title(c,b.enemy.name,120,435,48,TEXT,false);text(c,b.enemy.subtitle,120,482,24,MUTED,false);
        drawEnemySigil(c,790,585,b.enemy.pattern,b.enemy.boss);
        text(c,"HP "+b.enemy.hp+" / "+b.enemy.maxHp,120,565,27,TEXT,false);bar(c,120,595,480,28,b.enemy.hp/(float)b.enemy.maxHp,RED);
        smallCaps(c,"INTENT",120,680,MUTED,21,false);text(c,b.enemy.intentText(b.paradox),120,728,34,b.enemy.intentDamage(b.paradox)>0?RED:GOLD,false);
        if(b.enemy.weak>0)chip(c,120,790,"WEAK "+b.enemy.weak,CYAN);if(b.enemy.vulnerable>0)chip(c,300,790,"EXPOSED "+b.enemy.vulnerable,MAGENTA);
        panel(c,80,985,920,315,Color.argb(205,12,20,36),Color.argb(90,63,219,255));
        smallCaps(c,"ERA SYNCHRONY",110,1038,CYAN,21,false);
        String[] eras={"PAST","PRESENT","FUTURE"};int[] cols={CYAN,GOLD,MAGENTA};
        for(int i=0;i<3;i++){float x=125+i*285;boolean active=b.era==i;panel(c,x,1080,250,90,active?Color.argb(225,30,43,62):Color.argb(150,14,22,38),active?cols[i]:Color.argb(70,120,140,160));smallCaps(c,eras[i],x+125,1137,active?cols[i]:MUTED,23,true);}
        button(c,125,1190,180,74,"< SHIFT",CYAN,()->{sound.click();if(!b.shift(-1))toast("No Chrono or already at Past.");});
        text(c,"CHRONO "+b.chrono,540,1240,25,TEXT,true);
        button(c,775,1190,180,74,"SHIFT >",MAGENTA,()->{sound.click();if(!b.shift(1))toast("No Chrono or already at Future.");});
        text(c,"HULL "+g.hp+"/"+g.maxHp,100,1360,28,TEXT,false);bar(c,100,1380,310,24,g.hp/(float)g.maxHp,GREEN);
        text(c,"BLOCK "+b.block,450,1404,28,CYAN,false);text(c,"ENERGY "+b.energy,650,1404,28,GOLD,false);text(c,"PARADOX "+b.paradox+"/12",835,1404,26,b.paradox>=9?RED:b.paradox>=5?MAGENTA:TEXT,true);
        bar(c,815,1422,165,14,b.paradox/12f,b.paradox>=9?RED:MAGENTA);
        text(c,b.log,VW/2,1494,24,MUTED,true);
        int n=b.hand.size();float gap=n<=5?200:n<=7?145:112;float total=(n-1)*gap+185;float sx=(VW-total)/2;
        for(int i=0;i<n;i++){final int idx=i;float x=sx+i*gap;float y=b.selected==i?1560:1620;drawMiniCard(c,b.hand.get(i),x,y,185,390,b.selected==i);hits.add(new Hotspot(new RectF(x,y,x+185,y+390),()->{sound.click();b.selected=idx;}));}
        if(b.selected>=0&&b.selected<b.hand.size()){
            Card sel=b.hand.get(b.selected);drawSelectedSummary(c,sel);
            button(c,100,2135,330,100,"PLAY • "+sel.cost+" ENERGY",eraColor(sel.era),()->{int before=b.enemy.hp;if(b.play(b.selected)){sound.play();if(b.enemy.hp<before)sound.hit();}else toast("Not enough Energy.");});
        } else text(c,"Tap a card to inspect and play it.",100,2200,23,MUTED,false);
        button(c,455,2135,245,100,b.rewound?"REWOUND":"REWIND",b.rewound?MUTED:MAGENTA,()->{if(b.rewind()){sound.rewind();}else toast("This turn is already fixed in memory.");});
        button(c,725,2135,255,100,"END TURN",RED,()->{sound.click();b.endTurn();});
    }

    private void drawSelectedSummary(Canvas c,Card card){
        panel(c,84,1518,912,120,Color.argb(230,8,14,26),Color.argb(90,Color.red(eraColor(card.era)),Color.green(eraColor(card.era)),Color.blue(eraColor(card.era))));
        smallCaps(c,card.name,110,1562,eraColor(card.era),24,false);text(c,card.description(),110,1605,20,MUTED,false);
    }

    private void drawReward(Canvas c){
        runHeader(c,"SALVAGE");sound.setEnabled(sound.isEnabled());smallCaps(c,"CHOOSE ONE CARD",VW/2,360,GOLD,34,true);
        for(int i=0;i<g.rewardCards.size();i++){final Card card=g.rewardCards.get(i);float x=55+i*340;drawLargeCard(c,card,x,520,300,760);hits.add(new Hotspot(new RectF(x,520,x+300,1280),()->{sound.reward();g.takeRewardCard(card);}));}
        if(g.rewardRelic!=null){Relic r=g.rewardRelic;panel(c,130,1385,820,190,Color.argb(210,16,25,42),Color.argb(140,255,200,94));smallCaps(c,"ELITE SALVAGE • "+r.name,165,1440,GOLD,25,false);wrap(c,r.description,165,1482,750,24,TEXT,1.2f);text(c,"Relic is acquired after choosing or skipping a card.",165,1550,20,MUTED,false);}
        button(c,300,1695,480,105,"SKIP CARD",TEXT,()->{sound.click();g.skipReward();});
        text(c,"Credits secured: "+g.gold,VW/2,1865,27,MUTED,true);
    }

    private void drawEvent(Canvas c){
        runHeader(c,"CAUSAL EVENT");smallCaps(c,"UNSTABLE HISTORY",90,400,MAGENTA,25,false);title(c,g.eventTitle,90,500,52,TEXT,false);
        panel(c,75,610,930,470,Color.argb(190,13,21,36),Color.argb(100,233,88,255));wrap(c,g.eventBody,120,700,840,37,TEXT,1.38f);
        for(int i=0;i<2;i++){final int choice=i;button(c,110,1250+i*180,860,135,g.eventChoices[i],i==0?CYAN:GOLD,()->{sound.click();g.resolveEvent(choice);});}
        text(c,"Causal events can permanently alter this run.",VW/2,1720,25,MUTED,true);
    }

    private void drawShop(Canvas c){
        runHeader(c,"THE DEAD MARKET");smallCaps(c,"CREDITS • "+g.gold,90,360,GOLD,30,false);
        text(c,"Cards",90,435,31,TEXT,false);
        for(int i=0;i<g.shopCards.size();i++){final Card card=g.shopCards.get(i);float x=70+(i%3)*335,y=500+(i/3)*480;drawShopCard(c,card,x,y,300,410,g.cardPrice(card));hits.add(new Hotspot(new RectF(x,y,x+300,y+410),()->{if(g.buyCard(card)){sound.reward();}else toast("Insufficient Credits.");}));}
        float ry=1485;text(c,"Relics",90,ry,31,TEXT,false);for(int i=0;i<g.shopRelics.size();i++){final Relic r=g.shopRelics.get(i);float y=ry+45+i*180;panel(c,90,y,900,150,Color.argb(205,14,24,40),Color.argb(100,255,200,94));smallCaps(c,r.name,125,y+50,GOLD,23,false);wrap(c,r.description,125,y+80,650,21,MUTED,1.1f);text(c,g.relicPrice(r)+" CR",925,y+86,23,TEXT,true);hits.add(new Hotspot(new RectF(90,y,990,y+150),()->{if(g.buyRelic(r)){sound.reward();}else toast("Insufficient Credits.");}));}
        button(c,90,2035,410,105,"REMOVE BASIC • 65",TEXT,()->{if(g.removeCard()){sound.reward();toast("Basic card removed.");}else toast("Need 65 Credits and at least 6 cards.");});
        button(c,580,2035,410,105,"LEAVE MARKET",CYAN,()->{sound.click();g.completeNode();});
    }

    private void drawRest(Canvas c){
        runHeader(c,"ANCHOR STATION");title(c,"Repair the timeline.",90,470,55,TEXT,false);text(c,"Choose one service. Then this safe moment collapses behind you.",90,530,26,MUTED,false);
        panel(c,90,665,900,250,Color.argb(205,14,24,40),Color.argb(100,91,231,158));smallCaps(c,"HULL RECONSTRUCTION",130,735,GREEN,25,false);text(c,"Repair 30% of maximum hull.",130,795,27,TEXT,false);button(c,690,730,250,100,"REPAIR",GREEN,()->{sound.reward();g.restHeal();});
        smallCaps(c,"REFINE ONE CARD",90,1045,GOLD,28,false);text(c,"Upgrades increase values or improve card economy.",90,1095,24,MUTED,false);
        for(int i=0;i<g.upgradeCandidates.size();i++){final int deckIndex=g.upgradeCandidates.get(i);Card card=CardLibrary.get(g.deck.get(deckIndex));float x=70+i*335;drawLargeCard(c,card,x,1190,300,700);hits.add(new Hotspot(new RectF(x,1190,x+300,1890),()->{sound.reward();g.restUpgrade(deckIndex);}));}
        if(g.upgradeCandidates.isEmpty())text(c,"Every card in your deck is already refined.",VW/2,1450,30,MUTED,true);
        button(c,360,2045,360,100,"LEAVE",TEXT,()->{sound.click();g.completeNode();});
    }

    private void drawCodex(Canvas c){
        header(c,"CARD CODEX",CardLibrary.size()+" DISCOVERED",()->{g.screen=g.returnScreen;});List<Card> all=CardLibrary.all();int per=8,pages=(all.size()+per-1)/per;g.codexPage=Math.max(0,Math.min(pages-1,g.codexPage));
        smallCaps(c,"PAGE "+(g.codexPage+1)+" / "+pages,VW/2,330,MUTED,24,true);
        int startI=g.codexPage*per;for(int i=0;i<per&&startI+i<all.size();i++){Card card=all.get(startI+i);int col=i%2;int row=i/2;float x=65+col*505,y=410+row*405;drawCodexCard(c,card,x,y,455,360);}
        if(g.codexPage>0)button(c,70,2140,250,95,"PREVIOUS",TEXT,()->{sound.click();g.codexPage--;});
        if(g.codexPage<pages-1)button(c,760,2140,250,95,"NEXT",CYAN,()->{sound.click();g.codexPage++;});
    }

    private void drawDeck(Canvas c){
        header(c,"RUN DECK",g.deck.size()+" CARDS",()->{g.screen=g.returnScreen;});int per=10,pages=Math.max(1,(g.deck.size()+per-1)/per);deckPage=Math.max(0,Math.min(pages-1,deckPage));
        smallCaps(c,"PAGE "+(deckPage+1)+" / "+pages,VW/2,330,MUTED,24,true);int s=deckPage*per;
        for(int i=0;i<per&&s+i<g.deck.size();i++){Card card=CardLibrary.get(g.deck.get(s+i));float y=400+i*155;panel(c,70,y,940,135,Color.argb(205,13,22,38),Color.argb(75,Color.red(eraColor(card.era)),Color.green(eraColor(card.era)),Color.blue(eraColor(card.era))));
            text(c,String.valueOf(card.cost),112,y+80,42,GOLD,true);smallCaps(c,card.name,165,y+48,eraColor(card.era),23,false);text(c,card.description(),165,y+93,19,MUTED,false);smallCaps(c,card.era.name()+" • "+card.rarity,910,y+48,MUTED,16,true);}
        if(deckPage>0)button(c,70,2140,250,95,"PREVIOUS",TEXT,()->{sound.click();deckPage--;});if(deckPage<pages-1)button(c,760,2140,250,95,"NEXT",CYAN,()->{sound.click();deckPage++;});
    }

    private void drawStats(Canvas c){
        header(c,g.returnScreen==GameState.Screen.MAP?"RELIC VAULT":"CHRONICLE","PERSISTENT RECORD",()->{g.screen=g.returnScreen;});
        if(g.returnScreen==GameState.Screen.MAP){
            smallCaps(c,"CURRENT RELICS",80,350,GOLD,28,false);int per=7,pages=Math.max(1,(g.relics.size()+per-1)/per);relicPage=Math.max(0,Math.min(pages-1,relicPage));int s=relicPage*per;
            for(int i=0;i<per&&s+i<g.relics.size();i++){Relic r=RelicLibrary.get(g.relics.get(s+i));float y=430+i*220;panel(c,80,y,920,185,Color.argb(205,14,24,40),Color.argb(95,255,200,94));smallCaps(c,r.name,120,y+60,GOLD,26,false);wrap(c,r.description,120,y+98,800,23,TEXT,1.15f);smallCaps(c,r.rarity,925,y+60,MUTED,17,true);}
            if(relicPage>0)button(c,70,2140,250,95,"PREVIOUS",TEXT,()->{sound.click();relicPage--;});if(relicPage<pages-1)button(c,760,2140,250,95,"NEXT",CYAN,()->{sound.click();relicPage++;});
        }else{
            title(c,"Every failed future leaves data.",90,470,50,TEXT,false);String[][] stats={{"RUNS",String.valueOf(g.stat("runs"))},{"VICTORIES",String.valueOf(g.stat("wins"))},{"COLLAPSED TIMELINES",String.valueOf(g.stat("deaths"))},{"DEEPEST FLOOR",String.valueOf(g.stat("best_floor"))},{"CHRONICLE SHARDS",String.valueOf(g.stat("shards"))},{"CARDS IN CODEX",String.valueOf(CardLibrary.size())}};
            for(int i=0;i<stats.length;i++){float x=80+(i%2)*480,y=650+(i/2)*300;panel(c,x,y,440,235,Color.argb(190,13,22,38),Color.argb(70,63,219,255));smallCaps(c,stats[i][0],x+35,y+65,MUTED,20,false);title(c,stats[i][1],x+35,y+155,58,i==4?GOLD:TEXT,false);}
            panel(c,80,1650,920,270,Color.argb(180,13,22,38),Color.argb(70,233,88,255));smallCaps(c,"DESIGN PRINCIPLE",120,1720,MAGENTA,22,false);wrap(c,"Chronocrypt has no permanent stat grind. Chronicle Shards are a record of completed timelines, not a requirement to make later runs fair. Victory comes from deck construction and temporal play.",120,1770,830,24,TEXT,1.25f);
        }
    }

    private void drawGameOver(Canvas c,boolean win){
        smallCaps(c,win?"TIMELINE SECURED":"TIMELINE COLLAPSED",VW/2,480,win?GOLD:RED,34,true);title(c,win?"YOU REACHED\nTHE LAST SECOND":"CAUSALITY\nREJECTED YOU",VW/2,690,72,TEXT,true);
        stroke.setStrokeWidth(8);stroke.setColor(win?GOLD:RED);c.drawCircle(VW/2,1120,210,stroke);for(int i=0;i<12;i++){double a=i*Math.PI/6;c.drawLine(VW/2+(float)Math.cos(a)*155,1120+(float)Math.sin(a)*155,VW/2+(float)Math.cos(a)*210,1120+(float)Math.sin(a)*210,stroke);}
        text(c,win?"The Chronocrypt is quiet. For now.":"Your deck is gone, but the Chronicle remembers.",VW/2,1455,29,MUTED,true);
        if(win)text(c,"+"+(25+g.act*5)+" Chronicle Shards",VW/2,1515,31,GOLD,true);
        button(c,200,1700,680,115,"RETURN TO CHRONICLE",CYAN,()->{sound.click();g.screen=GameState.Screen.MENU;});
        button(c,200,1840,680,115,"BEGIN NEW TIMELINE",GOLD,()->{sound.reward();g.newRun();});
    }

    private void runHeader(Canvas c,String label){
        panel(c,0,0,VW,240,Color.argb(220,8,14,26),Color.argb(55,63,219,255));smallCaps(c,label,65,90,CYAN,27,false);text(c,"ACT "+g.act+"  /  FLOOR "+g.floor,65,145,24,MUTED,false);
        text(c,"HP "+g.hp+"/"+g.maxHp,1010,90,24,GREEN,true);text(c,"CR "+g.gold,1010,145,24,GOLD,true);
    }

    private void header(Canvas c,String label,String meta,Runnable back){panel(c,0,0,VW,235,Color.argb(220,8,14,26),Color.argb(55,63,219,255));button(c,45,62,160,92,"BACK",TEXT,()->{sound.click();back.run();});smallCaps(c,label,245,102,CYAN,30,false);text(c,meta,1015,102,22,MUTED,true);}

    private void drawMiniCard(Canvas c,Card card,float x,float y,float w,float h,boolean selected){
        int col=eraColor(card.era);panel(c,x,y,w,h,Color.argb(245,18,28,47),Color.argb(selected?245:150,Color.red(col),Color.green(col),Color.blue(col)));
        p.setColor(col);c.drawCircle(x+32,y+35,22,p);text(c,String.valueOf(card.cost),x+32,y+43,22,BG,true);smallCaps(c,card.era.name().substring(0,1),x+w-25,y+40,col,17,true);
        wrap(c,card.name,x+15,y+85,w-30,20,TEXT,1.1f);stroke.setColor(Color.argb(80,Color.red(col),Color.green(col),Color.blue(col)));c.drawLine(x+15,y+175,x+w-15,y+175,stroke);wrap(c,shortDesc(card),x+15,y+200,w-30,17,MUTED,1.15f);smallCaps(c,card.type.name(),x+15,y+h-25,col,14,false);
    }
    private String shortDesc(Card c){String d=c.description();return d.length()>85?d.substring(0,82)+"...":d;}

    private void drawLargeCard(Canvas c,Card card,float x,float y,float w,float h){int col=eraColor(card.era);panel(c,x,y,w,h,Color.argb(245,17,27,46),Color.argb(210,Color.red(col),Color.green(col),Color.blue(col)));p.setColor(col);c.drawCircle(x+45,y+50,30,p);text(c,String.valueOf(card.cost),x+45,y+61,29,BG,true);smallCaps(c,card.era.name(),x+w-20,y+55,col,18,true);wrap(c,card.name,x+25,y+115,w-50,27,TEXT,1.15f);stroke.setColor(Color.argb(90,Color.red(col),Color.green(col),Color.blue(col)));c.drawLine(x+25,y+255,x+w-25,y+255,stroke);wrap(c,card.description(),x+25,y+300,w-50,22,MUTED,1.28f);smallCaps(c,card.rarity+" • "+card.type.name(),x+w/2,y+h-35,col,16,true);}
    private void drawShopCard(Canvas c,Card card,float x,float y,float w,float h,int price){drawLargeCard(c,card,x,y,w,h-65);panel(c,x,y+h-70,w,70,Color.argb(245,10,17,29),Color.argb(90,255,200,94));text(c,price+" CR",x+w/2,y+h-24,22,GOLD,true);}
    private void drawCodexCard(Canvas c,Card card,float x,float y,float w,float h){int col=eraColor(card.era);panel(c,x,y,w,h,Color.argb(220,15,24,41),Color.argb(100,Color.red(col),Color.green(col),Color.blue(col)));p.setColor(col);c.drawCircle(x+38,y+42,23,p);text(c,String.valueOf(card.cost),x+38,y+50,21,BG,true);smallCaps(c,card.name,x+78,y+48,TEXT,21,false);smallCaps(c,card.era.name()+" • "+card.rarity,x+w-25,y+48,col,14,true);wrap(c,card.description(),x+25,y+105,w-50,20,MUTED,1.2f);}

    private void drawEnemySigil(Canvas c,float cx,float cy,int pattern,boolean boss){float t=(System.nanoTime()-start)/1_000_000_000f;int col=boss?GOLD:RED;stroke.setColor(Color.argb(180,Color.red(col),Color.green(col),Color.blue(col)));stroke.setStrokeWidth(boss?7:4);int sides=5+pattern;Path path=new Path();for(int i=0;i<sides;i++){double a=-Math.PI/2+i*Math.PI*2/sides+t*.08;float r=i%2==0?145:95;float x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();c.drawPath(path,stroke);stroke.setStrokeWidth(2);for(int i=0;i<3;i++)c.drawCircle(cx,cy,45+i*34+(float)Math.sin(t+i)*5,stroke);p.setColor(Color.argb(150,Color.red(col),Color.green(col),Color.blue(col)));c.drawCircle(cx,cy,18,p);}

    private void panel(Canvas c,float x,float y,float w,float h,int fill,int border){p.setStyle(Paint.Style.FILL);p.setColor(fill);c.drawRoundRect(x,y,x+w,y+h,22,22,p);stroke.setColor(border);stroke.setStrokeWidth(2.5f);c.drawRoundRect(x,y,x+w,y+h,22,22,stroke);}
    private void button(Canvas c,float x,float y,float w,float h,String label,int col,Runnable action){panel(c,x,y,w,h,Color.argb(230,14,24,41),Color.argb(190,Color.red(col),Color.green(col),Color.blue(col)));smallCaps(c,label,x+w/2,y+h/2+10,col,label.length()>24?19:23,true);hits.add(new Hotspot(new RectF(x,y,x+w,y+h),action));}
    private void chip(Canvas c,float x,float y,String s,int col){float w=24+s.length()*13;panel(c,x,y,w,55,Color.argb(190,15,23,38),Color.argb(120,Color.red(col),Color.green(col),Color.blue(col)));smallCaps(c,s,x+w/2,y+36,col,16,true);}
    private void bar(Canvas c,float x,float y,float w,float h,float v,int col){p.setColor(Color.rgb(31,42,58));c.drawRoundRect(x,y,x+w,y+h,h/2,h/2,p);p.setColor(col);c.drawRoundRect(x,y,x+w*Math.max(0,Math.min(1,v)),y+h,h/2,h/2,p);}
    private void title(Canvas c,String s,float x,float y,float size,int col,boolean center){p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));textLines(c,s,x,y,size,col,center,1.05f);}
    private void smallCaps(Canvas c,String s,float x,float y,int col,float size,boolean center){p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setLetterSpacing(.08f);text(c,s.toUpperCase(Locale.US),x,y,size,col,center);p.setLetterSpacing(0);}
    private void text(Canvas c,String s,float x,float y,float size,int col,boolean center){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(size);if(p.getTypeface()==null)p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);c.drawText(s,x,y,p);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));}
    private void textLines(Canvas c,String s,float x,float y,float size,int col,boolean center,float spacing){String[] lines=s.split("\\n");for(int i=0;i<lines.length;i++)text(c,lines[i],x,y+i*size*spacing,size,col,center);}
    private void wrap(Canvas c,String s,float x,float y,float width,float size,int col,float spacing){p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setTextAlign(Paint.Align.LEFT);p.setColor(col);String[] words=s.split(" ");String line="";float yy=y;for(String word:words){String test=line.isEmpty()?word:line+" "+word;if(p.measureText(test)>width&&!line.isEmpty()){c.drawText(line,x,yy,p);yy+=size*spacing;line=word;}else line=test;}if(!line.isEmpty())c.drawText(line,x,yy,p);}

    private int eraColor(Card.Era e){return e==Card.Era.PAST?CYAN:e==Card.Era.PRESENT?GOLD:MAGENTA;}
    private int nodeColor(GameState.NodeType n){switch(n){case ELITE:return MAGENTA;case EVENT:return CYAN;case SHOP:return GOLD;case REST:return GREEN;case BOSS:return RED;default:return TEXT;}}
    private String nodeGlyph(GameState.NodeType n){switch(n){case ELITE:return "E";case EVENT:return "?";case SHOP:return "$";case REST:return "+";case BOSS:return "X";default:return "!";}}
    private String nodeName(GameState.NodeType n){switch(n){case ELITE:return "ELITE";case EVENT:return "EVENT";case SHOP:return "MARKET";case REST:return "ANCHOR";case BOSS:return "BOSS";default:return "COMBAT";}}
    private String nodeHint(GameState.NodeType n){switch(n){case ELITE:return "Dangerous anomaly. Relic guaranteed.";case EVENT:return "A broken piece of history. Outcomes vary.";case SHOP:return "Spend Credits. Thin or expand the deck.";case REST:return "Repair hull or refine one card.";case BOSS:return "A timeline-defining encounter.";default:return "Standard hostile. Card salvage on victory.";}}

    private void toast(String s){toast=s;toastUntil=System.currentTimeMillis()+1900;invalidate();}
    private void drawToast(Canvas c){panel(c,150,2245,780,90,Color.argb(245,10,17,29),Color.argb(180,255,200,94));text(c,toast,VW/2,2302,22,TEXT,true);}

    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=(e.getX()-offX)/scale,y=(e.getY()-offY)/scale;
        for(int i=hits.size()-1;i>=0;i--){Hotspot h=hits.get(i);if(h.r.contains(x,y)){performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);h.a.run();invalidate();return true;}}
        return true;
    }
}
