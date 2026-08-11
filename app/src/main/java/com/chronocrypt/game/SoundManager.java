package com.chronocrypt.game;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

public final class SoundManager {
    private final ToneGenerator tone;
    private boolean enabled=true;
    public SoundManager(Context c){tone=new ToneGenerator(AudioManager.STREAM_MUSIC,45);}
    private void p(int kind,int ms){if(enabled)tone.startTone(kind,ms);}
    public void click(){p(ToneGenerator.TONE_PROP_BEEP,45);}
    public void play(){p(ToneGenerator.TONE_PROP_ACK,80);}
    public void hit(){p(ToneGenerator.TONE_PROP_NACK,70);}
    public void reward(){p(ToneGenerator.TONE_PROP_PROMPT,130);}
    public void rewind(){p(ToneGenerator.TONE_SUP_RINGTONE,180);}
    public void setEnabled(boolean b){enabled=b;} public boolean isEnabled(){return enabled;}
    public void release(){tone.release();}
}
