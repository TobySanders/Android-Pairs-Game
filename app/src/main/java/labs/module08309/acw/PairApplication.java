package labs.module08309.acw;

import android.app.Application;
import android.media.MediaPlayer;

/**
 * Created by Toby on 11/03/2016.
 */
public class PairApplication extends Application {

    //g_ prefix indicates variable is globally accessible

    private static Boolean g_PlayMusic = false;
    private static MediaPlayer g_MusicPlayer;

    public MediaPlayer getG_MusicPlayer(){
        return g_MusicPlayer;
    }
    public void setG_MusicPlayer(MediaPlayer pPlayer){
        g_MusicPlayer = pPlayer;
    }
    public void startMusicPlayer(MediaPlayer pPlayer){
        g_MusicPlayer = pPlayer;
        g_PlayMusic = true;
        g_MusicPlayer.setLooping(true);
        startMusic();
    }
    private static void startMusic(){
        g_MusicPlayer.start();
    }
    private static void pauseMusic(){
        g_MusicPlayer.pause();
    }


    public static Boolean getG_PlayMusic(){
        return g_PlayMusic;
    }
    public static void setG_PlayMusic(boolean pVal){
        g_PlayMusic = pVal;
        if(!g_PlayMusic && g_MusicPlayer.isPlaying())
            pauseMusic();
        else if(g_PlayMusic && !g_MusicPlayer.isPlaying())
            startMusic();
    }

    public static void onLostFocus(){
        if(g_MusicPlayer != null && g_MusicPlayer.isPlaying())
            g_MusicPlayer.pause();
    }

    public static void onGainedFocus(){
        setG_PlayMusic(g_PlayMusic);
    }
}
