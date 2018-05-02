package labs.module08309.acw;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Toby on 04/04/2016.
 */
public class LevelSimpleActivity extends AppCompatActivity implements LevelSimpleFragment.GameUpdateListener,
        GameOverDialog.DialogAnswerListener,
        ScorerFragment.ScorerUpdateListener{

    private LevelSimpleFragment m_LevelSimpleFragment;
    private ScorerFragment m_ScorerFragment;
    private FragmentManager m_FragmentManager;
    private String m_LevelName;
    private boolean GAME_IS_RUNNING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_FragmentManager = getFragmentManager();
        int rotation = ((WindowManager) getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();


        switch (rotation) {
            case Surface.ROTATION_0:
                setContentView(R.layout.activity_level_simple_portrait);
                break;
            case Surface.ROTATION_90:
                setContentView(R.layout.activity_level_simple_landscape);
                break;
            case Surface.ROTATION_180:
                setContentView(R.layout.activity_level_simple_portrait);
                break;
            default:
                setContentView(R.layout.activity_level_simple_landscape);
                break;
        }

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
         getSupportActionBar().hide();


        if(savedInstanceState == null) {
            m_ScorerFragment = new ScorerFragment();
            m_FragmentManager.beginTransaction()
                    .add(R.id.layout_levelSimple_scorer,
                            m_ScorerFragment).commit();
            m_LevelName = getIntent().getStringExtra("level");

            m_LevelSimpleFragment = new LevelSimpleFragment();
            Bundle args = new Bundle();
            args.putString("level", m_LevelName);
            m_LevelSimpleFragment.setArguments(args);
            m_FragmentManager.beginTransaction().
                    add(R.id.layout_levelSimple, m_LevelSimpleFragment).commit();
        }
        else{
            m_ScorerFragment =(ScorerFragment)m_FragmentManager
                    .getFragment(savedInstanceState, "m_ScorerFragment");
            GAME_IS_RUNNING = savedInstanceState.getBoolean("GAME_IS_RUNNING");
            m_LevelName = savedInstanceState.getString("m_LevelName");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        getFragmentManager()
                .putFragment(savedInstanceState, "m_ScorerFragment", m_ScorerFragment);
        savedInstanceState.putBoolean("GAME_IS_RUNNING",GAME_IS_RUNNING);
        savedInstanceState.putString("m_LevelName", m_LevelName);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(GAME_IS_RUNNING)
            m_ScorerFragment.onGameResume();
        PairApplication.onGainedFocus();
    }

    @Override
    public void onTimeOut(int score) {
        m_ScorerFragment.onGamePause();
        SaveLevel(score, 0l, false);
        gameOver(score,false);
    }

    @Override
    public void onGameOver(int score) {
        m_ScorerFragment.onGamePause();
        SaveLevel(score,m_ScorerFragment.m_TimeRemaining,true);
        gameOver(score,true);
    }

    @Override
    public void onPause(){
        super.onPause();
        m_ScorerFragment.onGamePause();
        PairApplication.onLostFocus();
    }
    private void gameOver(int score, boolean win) {
        GameOverDialog gameOverDialog = new GameOverDialog();
        Bundle b = new Bundle();
        b.putInt("score", score);
        b.putBoolean("win", win);
        gameOverDialog.setArguments(b);
        gameOverDialog.setCancelable(false);
        /*
        Don't ask me why the disgusting following code is needed it just is, I started with the
        dialog.show which is commented out below and that worked for a bit then one day decided to
        just constantly throw exceptions; after browsing stack overflow for a while looking for
        solutions to the exception the only answer seemed to be
        "This is a known bug, override to allow state loss and surround in try catch"
        it's gross but the program functions completely the same even though the exception still
         occurs ...... android
         */
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(gameOverDialog, "dialog_GameOver");
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //gameOverDialog.show(m_FragmentManager, "dialog_GameOver");
    }

    @Override
    public void onScoreChanged(int score) {
        m_ScorerFragment.updateScore(score);
    }

    @Override
    public void onGameStart() {
        GAME_IS_RUNNING = true;
        m_ScorerFragment.onGameStart();
    }

    
    
    private void SaveLevel(int score, Long timeRemaining, Boolean levelComplete){

        File dir = getDir("levels",MODE_PRIVATE);
        File file = new File(dir,m_LevelName);

        FileInputStream fileInputStream = null;
        String levelJSON = "";
        try {
            fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            levelJSON = stringBuilder.toString();
        } catch (Exception ignored) {
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONObject scoreObject = new JSONObject();
        FileOutputStream writer = null;
        try {
            JSONObject levelObject= new JSONObject(levelJSON);
            JSONObject previousScoreObject;
            if (!levelObject.isNull("PuzzleResult")) {
                previousScoreObject = levelObject.getJSONObject("PuzzleResult");
                int previousScore = previousScoreObject.getInt("score");
                Boolean previousComplete = previousScoreObject.getBoolean("complete");
                long previousTime = previousScoreObject.getLong("timeRemaining");
            /*
                if the user has entered a higher score than before or has completed the level
                or has a faster time
                this attempt update the file, else ignore
             */
                if (score > previousScore ||
                        (!previousComplete && levelComplete) ||
                        (previousComplete && timeRemaining > previousTime)) {
                    scoreObject.put("score", score);
                    scoreObject.put("timeRemaining", timeRemaining);
                    scoreObject.put("complete", levelComplete);
                    levelObject.put("PuzzleResult", scoreObject);

                    writer = new FileOutputStream(file);
                    byte[] output = levelObject.toString().getBytes();
                    writer.write(output, 0, output.length);
                }
            }
            else{
                scoreObject.put("score", score);
                scoreObject.put("timeRemaining", timeRemaining);
                scoreObject.put("complete", levelComplete);
                levelObject.put("PuzzleResult", scoreObject);
                writer = new FileOutputStream(file);
                byte[] output = levelObject.toString().getBytes();
                writer.write(output, 0, output.length);
            }
        }
        catch (Exception e){
            Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT).show();}
        finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onAnswer(int response) {
        switch (response){
            case GameOverDialog.PLAY_AGAIN:
                setResult(Activity.RESULT_OK); //We want another Game
                finish();
                break;
            case GameOverDialog.MAIN_MENU:
                setResult(Activity.RESULT_CANCELED); //Close the Game tab
                finish();
                break;
            default:
                new Exception("Error in game over Dialog").printStackTrace();
                break;
        }
    }
}
