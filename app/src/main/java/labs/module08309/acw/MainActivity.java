package labs.module08309.acw;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button m_ButtonPlay;
    private Button m_ButtonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_ButtonPlay =  (Button)findViewById(R.id.button_Play);
        m_ButtonSettings = (Button)findViewById(R.id.button_Settings);
        if(savedInstanceState == null)
            ((PairApplication)this.getApplication()).startMusicPlayer(MediaPlayer.create(this, R.raw.game_backing));
    }
    public void playButtonOnClick(View view) {
        Intent intent = new Intent(this,LevelSelectActivity.class);
        startActivity(intent);
    }
    public void settingsButtonOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PairApplication.onGainedFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PairApplication.onLostFocus();
    }

}
