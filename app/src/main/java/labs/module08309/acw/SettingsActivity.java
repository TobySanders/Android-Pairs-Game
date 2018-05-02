package labs.module08309.acw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;

/**
 * Created by Toby on 11/03/2016.
 */
public class SettingsActivity extends AppCompatActivity {
    private Switch m_MusicSwitch;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        m_MusicSwitch = (Switch)findViewById(R.id.switch_Music);
        m_MusicSwitch.setChecked(((PairApplication)this.getApplication()).getG_PlayMusic());
    }
    public void musicSwitchOnClick(View pView){
        ((PairApplication)this.getApplication()).setG_PlayMusic(m_MusicSwitch.isChecked());
    }

    @Override
    protected void onPause() {
        super.onPause();
        PairApplication.onLostFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PairApplication.onGainedFocus();
    }
}
