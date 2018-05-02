package labs.module08309.acw;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Toby on 16/03/2016.
 */
public class LevelSimpleFragment extends Fragment{

    private Context m_Context;
    private GameUpdateListener m_GameUpdateListener;
    static final String DEBUG_TAG = "Level Simple Fragment";
    private static final int MATCH_BONUS = 40000;
    private static final int FAIL_MALUS = MATCH_BONUS /2;
    private static final int CHECK_MALUS = MATCH_BONUS /4;
    private static final int NUMBER_OF_MATCHES = 2;
    private static final int WAITING_TIME = 7000;
    private static final int ANIMATION_TIME = 700;

    private class DimensionSet{
        public int height, width;
        public DimensionSet(int height, int width){
            this.height = height;
            this.width = width;
        }
    }

    private final int CARD_MARGIN_TOP = 700;
    private final int CARD_MARGIN_SIDE = 250;
    private int m_Card_margin_top = 0;
    private int m_Card_margin_side = 0;
    private boolean m_Redrawn ;
    private int m_flipped_count = 0;
    private int m_total_matched = 0;
    private int m_total_toMatch;
    private int m_rowCount;
    private int m_columnCount;
    private int m_score;
    private ArrayList<MyViewFlipper> m_Held;
    private ArrayList<MyViewFlipper> m_CardFlippers;
    private ArrayList<MyViewFlipper> m_Flipped;
    private GridLayout m_GridLayout;
    private String m_LevelJSON;


    public interface GameUpdateListener {
        void onGameOver(int score);
        void onScoreChanged(int score);
        void onGameStart();
    }
    @Override
    public void onAttach(Context context){
        m_Context = context;
        try{
            m_GameUpdateListener = (GameUpdateListener) m_Context;

        }catch (ClassCastException e){
            throw new ClassCastException(m_Context.toString() +
                    "must implement ListSelectionListener and ProgressListener");
        }
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        super.onPause();
        m_Redrawn = true;
    }

    @Override
    public void onResume(){
        super.onResume();

        if(!m_Redrawn)
            new startUpTask().execute();
        else{
            int rotation = ((WindowManager) m_Context.getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            DimensionSet screenDimensions = getScreenDimensions();
            DimensionSet cardDimensions = getScaledDimensions(screenDimensions,rotation);

            for (MyViewFlipper viewFlipper: m_CardFlippers){

                //viewFlipper.showNext();
                viewFlipper.setLayoutParams(new ViewGroup.LayoutParams(cardDimensions.width
                        , cardDimensions.height));
                ImageView image1 = (ImageView) viewFlipper.getChildAt(0);
                ImageView image2 = (ImageView) viewFlipper.getChildAt(1);

                image1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                image1.getLayoutParams().width = cardDimensions.width;
                image1.getLayoutParams().height = cardDimensions.height;

                image2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                image2.getLayoutParams().width = cardDimensions.width;
                image2.getLayoutParams().height = cardDimensions.height;

                viewFlipper.removeView(image1);
                viewFlipper.removeView(image2);

                viewFlipper.addView(image1);
                viewFlipper.addView(image2);

                viewFlipper.showNext();

                viewFlipper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkFlipCount((MyViewFlipper) v);
                    }
                });
                m_GridLayout.addView(viewFlipper);
            }
            for (MyViewFlipper flipper:m_Flipped){
                flipper.showNext();
                flipper.setClickable(false);
            }
            for (MyViewFlipper flipper: m_Held){
                flipper.showNext();
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_level_simple, container, false);

        m_GridLayout = (GridLayout) view.findViewById(R.id.grid_levelGrid);

        if (savedInstanceState == null) {

            m_CardFlippers = new ArrayList<>();
            m_Held = new ArrayList<>();
            m_Flipped = new ArrayList<>();

            Bundle args = getArguments();
            String fileName = args.getString("level");

            assert fileName != null;
            File levelFile = new File(m_Context.getDir("levels", m_Context.MODE_PRIVATE), fileName);

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(levelFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);

                }
                m_LevelJSON = stringBuilder.toString();
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

            Card[] cards = ParseJson(m_LevelJSON);
            DrawCards(cards);
            m_Flipped = new ArrayList<>();
        }
        else {
            m_rowCount = savedInstanceState.getInt("m_RowCount");
            m_columnCount = savedInstanceState.getInt("m_ColumnCount");

            m_Card_margin_side = savedInstanceState.getInt("m_Card_margin_side");
            m_Card_margin_top = savedInstanceState.getInt("m_Card_margin_top");

            m_GridLayout.setRowCount(m_rowCount);
            m_GridLayout.setColumnCount(m_columnCount);

            m_score = savedInstanceState.getInt("m_Score");

            m_Flipped = (ArrayList<MyViewFlipper>)savedInstanceState.getSerializable("m_Flipped");
            m_Held = (ArrayList<MyViewFlipper>)savedInstanceState.getSerializable("m_Held");
            m_CardFlippers = (ArrayList<MyViewFlipper>)savedInstanceState.getSerializable("m_CardFlippers");

            m_flipped_count = savedInstanceState.getInt("m_FlippedCount");
            m_total_matched = savedInstanceState.getInt("m_TotalMatched");
            m_total_toMatch = savedInstanceState.getInt("m_TotalToMatch");
            m_Redrawn = true;
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("m_Flipped", m_Flipped);
        savedInstanceState.putSerializable("m_Held", m_Held);
        savedInstanceState.putSerializable("m_CardFlippers", m_CardFlippers);
            for (MyViewFlipper viewFlipper : m_CardFlippers) {
                m_GridLayout.removeView(viewFlipper);
                viewFlipper.setOnClickListener(null);
            }
        savedInstanceState.putInt("m_FlippedCount", m_flipped_count);
        savedInstanceState.putInt("m_TotalMatched", m_total_matched);
        savedInstanceState.putInt("m_TotalToMatch", m_total_toMatch);
        savedInstanceState.putInt("m_ColumnCount",m_columnCount);
        savedInstanceState.putInt("m_RowCount", m_rowCount);
        savedInstanceState.putInt("m_Score", m_score);
        savedInstanceState.putInt("m_Card_margin_top",m_Card_margin_top);
        savedInstanceState.putInt("m_Card_margin_side", m_Card_margin_side);

    }
    private Card[] ParseJson(String JSON){
        Card[] cardImages = null;
        JSONObject object;
        String pictureSet;
        try{
            object = new JSONObject(JSON);
            JSONObject jsonObject = object.getJSONObject("Puzzle");

            m_columnCount = jsonObject.getInt("Columns");
            m_Card_margin_side = CARD_MARGIN_SIDE/m_columnCount;

            m_rowCount = jsonObject.getInt("Rows");
            m_Card_margin_top = CARD_MARGIN_TOP/m_rowCount;

            m_GridLayout.setColumnCount(m_columnCount);
            m_GridLayout.setRowCount(m_rowCount);

            cardImages = new Card[m_rowCount*m_columnCount];


            pictureSet = jsonObject.getString("PictureSet");
            File pictureSetFile = new File(m_Context.getDir("picturesets",m_Context.MODE_PRIVATE), pictureSet);
            String pictureSetText = null;

            FileInputStream fileInputStream= null;
            try {
                fileInputStream = new FileInputStream(pictureSetFile);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine())!= null){
                    stringBuilder.append(line);
                }
                pictureSetText = stringBuilder.toString();
            }
            catch (Exception ignored){}finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(pictureSetText != null) {
                JSONObject rawObject = new JSONObject(pictureSetText);
                JSONObject pictureObject = rawObject.getJSONObject("PictureSet");
                JSONArray pictureArray = pictureObject.optJSONArray("Files");

                JSONArray JSONCards = jsonObject.optJSONArray("Layout");

                File fileDir = m_Context.getDir("images", m_Context.MODE_PRIVATE);
                File fileImage;
                for (int i = 0; i < JSONCards.length(); i++) {
                    fileImage = new File(fileDir,pictureArray.getString(JSONCards.getInt(i) -1));
                    try {

                        cardImages[i] = new Card(pictureArray.getString(JSONCards.getInt(i) -1),
                                BitmapFactory.decodeStream(new FileInputStream(fileImage)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (JSONException e){e.printStackTrace();}
        return cardImages;
    }


    private void DrawCards(Card[] cardImages) {
        m_total_toMatch = cardImages.length;
        int i = 0;
        int rotation = ((WindowManager) m_Context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        DimensionSet screenDimensions = getScreenDimensions();
        DimensionSet cardDimensions = getScaledDimensions(screenDimensions,rotation);
        for (Card cardImage: cardImages){
            final MyViewFlipper viewFlipper = new MyViewFlipper(m_Context);

            viewFlipper.setLayoutParams(new ViewGroup.LayoutParams(cardDimensions.width
                    ,cardDimensions.height));

            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(m_Context, R.anim.to_middle));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(m_Context, R.anim.from_middle));
            viewFlipper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkFlipCount(viewFlipper);
                }
            });
            /*add imageViews*/
            ImageView IV = new ImageView(m_Context);
            IV.setImageBitmap(cardImage.m_res);
            IV.setContentDescription(cardImage.m_value);
            IV.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            IV.setLayoutParams(new ViewGroup.LayoutParams(cardDimensions.width,
                    cardDimensions.height));
            viewFlipper.addView(IV);
            ImageView IV1 = new ImageView(m_Context);
            IV1.setImageResource(R.drawable.cardback);
            IV1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            IV1.setLayoutParams(new ViewGroup.LayoutParams(cardDimensions.width,
                    cardDimensions.height));
            viewFlipper.addView(IV1);

            m_GridLayout.addView(viewFlipper);
            m_CardFlippers.add(viewFlipper);
            i++;
        }
    }
    private void checkFlipCount(MyViewFlipper viewFlipper){

        //Check in this section for clicking a card that's already selected
        if(!m_Held.isEmpty()) {
            if (viewFlipper == m_Held.get(0)) {
                m_Held.get(0).showNext();//flip round
                m_Held.remove(0);   //clear the card

                if (m_Held.size() == 2) //check if we're holding a second
                {
                    m_Held.remove(0);
                    m_Held.add(0, m_Held.get(1));// move the second to first
                    m_Held.remove(1);//clear the second for checks

                }
                m_flipped_count--; // reduce flip counter
                m_score -= CHECK_MALUS;
                m_GameUpdateListener.onScoreChanged(m_score);
                return;
            } else if (m_Held.size() == 2) {
                if (viewFlipper == m_Held.get(1)) {
                    m_Held.get(1).showNext(); //flip card
                    m_Held.remove(1); //clear the card;
                    m_flipped_count--; // reduce flip counter
                    m_score -= CHECK_MALUS;
                    m_GameUpdateListener.onScoreChanged(m_score);
                    return;
                }
            }
        }

        //Check if selecting this new card triggers a match check
            if(m_flipped_count + 1 < NUMBER_OF_MATCHES){
                m_Held.add(viewFlipper);
                viewFlipper.showNext();
                m_flipped_count++;
            }
            else if(m_flipped_count + 1 == NUMBER_OF_MATCHES){ //This prevents the user spamming card flips while the animation plays

                m_Held.add(viewFlipper);
                m_flipped_count++;
                viewFlipper.showNext();
                new sleepTask().execute();
            }
    }
    private boolean checkMatch(){
        ImageView child0 = (ImageView) m_Held.get(0).getCurrentView();
        ImageView child1 = (ImageView) m_Held.get(1).getCurrentView();

        return child0.getContentDescription().equals(child1.getContentDescription());
    }

    private void lockOrientation() {
        int orientation;
        int rotation = ((WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                break;
        }
        getActivity().setRequestedOrientation(orientation);
    }


    private DimensionSet getScreenDimensions(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) m_Context.getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return new DimensionSet(displayMetrics.heightPixels,displayMetrics.widthPixels);
    }
    private DimensionSet getScaledDimensions(DimensionSet dimensionSet, int rotation) {
        int scaledHeight;
        int scaledWidth;
        final int BUFFER = 100;

        switch (rotation) {
            case Surface.ROTATION_0:
                scaledHeight = (dimensionSet.height / m_rowCount) - m_Card_margin_top;
                scaledWidth = (dimensionSet.width / m_columnCount) - m_Card_margin_side;
                break;
            case Surface.ROTATION_90:
                scaledHeight = (dimensionSet.height / m_rowCount) - m_Card_margin_side;
                scaledWidth = (dimensionSet.width / m_columnCount) - (m_Card_margin_top + BUFFER/m_columnCount);
                break;
            case Surface.ROTATION_180:
                scaledHeight = (dimensionSet.height / m_rowCount) - m_Card_margin_top;
                scaledWidth = (dimensionSet.width / m_columnCount) - m_Card_margin_side;
                break;
            default:
                scaledHeight = (dimensionSet.height / m_rowCount) - m_Card_margin_side;
                scaledWidth = (dimensionSet.width / m_columnCount) - (m_Card_margin_top + BUFFER/m_columnCount);
                break;
        }
        return new DimensionSet(scaledHeight, scaledWidth);
    }

    private class sleepTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute(){

        /*If the user rotates during the sleeping animation everything gets redrawn and we lose all
                    our references to the viewflippers mid way through the flip
                    this kills the activity
                    Lock the orientation and unlock after the animation has completed
                 */
            lockOrientation();
            //Make Uninteractable
            for (int i = 0; i < NUMBER_OF_MATCHES; i++){
                m_Held.get(i).setClickable(false);
            }
        }
        protected String doInBackground(String...params){
            try{Thread.sleep(ANIMATION_TIME);}catch(InterruptedException ignored){}
            return "yes";
        }
        @Override
        protected void onPostExecute(String result){

            if(checkMatch()){

                //clear the held list
                for (int i = 0; i < NUMBER_OF_MATCHES; i++){
                    m_Flipped.add(m_Held.get(0));
                    m_Held.remove(0);
                }

                //reset counter
                m_flipped_count = 0;

                //List the matches
                m_total_matched += NUMBER_OF_MATCHES;

                m_score += MATCH_BONUS;
            }
            else{
                //make interactable
                for (int i = 0; i < NUMBER_OF_MATCHES; i++){
                    m_Held.get(i).setClickable(true);
                }

                //flip cards
                for (int i = 0; i < NUMBER_OF_MATCHES; i++){
                    m_Held.get(i).showNext();
                }


                //clear held list
                for (int i = 0; i < NUMBER_OF_MATCHES; i++){
                    m_Held.remove(0);
                }

                //reset counter
                m_flipped_count = 0;

                m_score -= FAIL_MALUS;
            }
            m_GameUpdateListener.onScoreChanged(m_score);
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            if(m_total_matched == m_total_toMatch ){
                m_GameUpdateListener.onGameOver(m_score);
            }
        }
    }
    private class startUpTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute(){
            lockOrientation();
            //Make Uninteractable
            for (ViewFlipper flipper: m_CardFlippers){
                flipper.setClickable(false);
            }
        }
        protected String doInBackground(String...params){
            try{Thread.sleep(WAITING_TIME);}catch(InterruptedException ignored){}
            return "yes";
        }
        @Override
        protected void onPostExecute(String pResult){
            for (ViewFlipper flipper: m_CardFlippers){
                flipper.showNext();
                flipper.setClickable(true);
            }
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            m_GameUpdateListener.onGameStart();
        }}
}


