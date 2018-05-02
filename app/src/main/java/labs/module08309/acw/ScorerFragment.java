package labs.module08309.acw;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Toby on 16/03/2016.
 */
public class ScorerFragment extends Fragment{
    private Context m_Context;
    private TextView m_Score_TextView;
    private TextView m_Time_TextView;
    private ScorerUpdateListener m_ScorerUpdateListener;
    private final long LEVEL_TIME = 90000; //time in ms   60000 = 1 min
    private int m_CurrentScore = 0;
    long m_TimeRemaining = LEVEL_TIME;
    private long m_StartTimer = 0;
    private boolean GAME_IS_RUNNING = false;

    public interface ScorerUpdateListener{
        void onTimeOut(int score);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        m_Context = context;
    }

    public void updateScore(int score) {
        m_CurrentScore = score;
        m_Score_TextView.setText(String.valueOf(m_CurrentScore));
    }

    public void onGameStart(){
        GAME_IS_RUNNING = true;
        onGameResume();
    }
    public void onGamePause() {
        m_TimerHandler.removeCallbacks(m_TimerRunnable);
    }

    public void onGameResume() {
        m_StartTimer = System.currentTimeMillis() + m_TimeRemaining;
        m_TimerHandler.postDelayed(m_TimerRunnable, 0);
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putLong("m_TimeRemaining", m_TimeRemaining);
        savedInstanceState.putInt("m_CurrentScore", m_CurrentScore);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        try {
            m_ScorerUpdateListener = (ScorerUpdateListener)
                    m_Context;
        }catch (ClassCastException e){
            throw new ClassCastException("Must implement ScorerUpdateListener"
                    +e.toString());
        }
        m_Score_TextView.setText(String.valueOf(m_CurrentScore));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_scorer, container, false);
        m_Score_TextView = (TextView)view.findViewById(R.id.text_scorer_score);
        m_Time_TextView = (TextView)view.findViewById(R.id.text_scorer_timer);

        if(savedInstanceState != null){
            m_TimeRemaining = savedInstanceState.getLong("m_TimeRemaining");
            m_CurrentScore = savedInstanceState.getInt("m_CurrentScore");
        }
        else
            m_Time_TextView.setText(StringFormatter.FormatTime(m_TimeRemaining));
        if(GAME_IS_RUNNING)
            onGameResume();
        return view;
    }

    private Handler m_TimerHandler = new Handler();
    private Runnable m_TimerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = m_StartTimer - System.currentTimeMillis();
            m_Time_TextView.setText(StringFormatter.FormatTime(millis));
            m_TimeRemaining = millis;
            if(m_TimeRemaining <= 0)
                m_ScorerUpdateListener.onTimeOut(m_CurrentScore);
            else
                m_TimerHandler.postDelayed(this, 500);
        }
    };
}

