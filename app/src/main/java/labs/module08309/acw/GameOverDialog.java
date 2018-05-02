package labs.module08309.acw;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Toby on 16/03/2016.
 */
public class GameOverDialog extends DialogFragment {

    public static final int PLAY_AGAIN = 0;
    public static final int MAIN_MENU = 1;
    private DialogAnswerListener m_DialogAnswer;

    public GameOverDialog() {
    }

    public interface DialogAnswerListener {
         void onAnswer(int response);
        /*
            0 = play again
            1 = Main menu

            Thought it was a waste creating an enum for something so trivial
            and a boolean seemed a bit obscure
         */
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            m_DialogAnswer = (DialogAnswerListener) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ListSelectionListener and ProgressListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        View view = inflater.inflate(R.layout.dialog_game_over, container);

        int score = args.getInt("score");
        boolean win = args.getBoolean("win");

        TextView scoreText = (TextView)view.findViewById(R.id.text_GameOver_Score);
        TextView titleText = (TextView)view.findViewById(R.id.label_GameOver_LevelComplete);

        if(win)
            titleText.setText(getString(R.string.text_GameOver_title_win));
        else
            titleText.setText(getString(R.string.text_GameOver_title_lose));
        scoreText.setText(String.valueOf(score));

        Button playAgainButton = (Button)view.findViewById(R.id.button_GameOver_PlayAgain);
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    m_DialogAnswer.onAnswer(PLAY_AGAIN);
            }
        });
        Button mainMenuButton = (Button)view.findViewById(R.id.button_GameOver_MainMenu);
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    m_DialogAnswer.onAnswer(MAIN_MENU);
            }
        });

        super.onCreate(savedInstanceState);
        return view;
    }
}

