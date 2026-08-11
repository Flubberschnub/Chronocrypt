package com.chronocrypt.game;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.lang.reflect.Field;

public final class MainActivity extends Activity {
    private static final String PREFS = "chronocrypt_v1";
    private static final String INTERRUPTED_NODE = "interrupted_node";
    private ChronoGameView gameView;
    private SharedPreferences prefs;
    private boolean resumedOnce;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        recoverInterruptedNode();
        immersive();
        gameView=new ChronoGameView(this);
        setContentView(gameView);
    }

    private void recoverInterruptedNode(){
        if(!prefs.getBoolean(INTERRUPTED_NODE,false)) return;
        if(prefs.getBoolean("run_active",false)){
            int floor=prefs.getInt("floor",0);
            prefs.edit().putInt("floor",Math.max(0,floor-1)).remove(INTERRUPTED_NODE).apply();
        }else{
            prefs.edit().remove(INTERRUPTED_NODE).apply();
        }
    }

    private void immersive(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private GameState currentState(){
        if(gameView==null) return null;
        try{
            Field f=ChronoGameView.class.getDeclaredField("g");
            f.setAccessible(true);
            return (GameState)f.get(gameView);
        }catch(Exception ignored){
            return null;
        }
    }

    private boolean isTransientRunNode(){
        GameState s=currentState();
        if(s==null||!s.activeRun) return false;
        switch(s.screen){
            case COMBAT:
            case REWARD:
            case EVENT:
            case SHOP:
            case REST:
                return true;
            default:
                return false;
        }
    }

    @Override protected void onResume(){
        super.onResume();
        if(resumedOnce) prefs.edit().remove(INTERRUPTED_NODE).apply();
        resumedOnce=true;
        immersive();
    }

    @Override public void onWindowFocusChanged(boolean f){super.onWindowFocusChanged(f);if(f)immersive();}

    @Override protected void onPause(){
        if(isTransientRunNode()){
            prefs.edit().putBoolean(INTERRUPTED_NODE,true).apply();
        }else if(gameView!=null){
            gameView.persist();
        }
        super.onPause();
    }

    @Override protected void onDestroy(){if(gameView!=null)gameView.destroy();super.onDestroy();}
    @Override public void onBackPressed(){if(gameView!=null&&gameView.handleBack())return;super.onBackPressed();}
}
