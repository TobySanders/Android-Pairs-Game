package labs.module08309.acw;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Toby on 16/03/2016.
 */

public class LevelSelectFragment extends Fragment{

    private ProgressBar m_ProgressBar;
    private ListSelectionListener m_Listener = null;
    private ListView m_ListView;
    private Context m_Context;
    private ArrayList<LevelObject> m_Levels;
    private SortType m_CurrentSortType = SortType.ScoreHI;

    public enum SortType {
        ScoreHI,
        ScoreLO,
        RowsHI,
        RowsLO,
        ColumnsHI,
        ColumnsLO,
        Name,
        Completed,
        Uncompleted
    }

    public interface ListSelectionListener{
        void onListSelection(String selected);
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        m_Context = context;
        try{
            m_Listener = (ListSelectionListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    "must implement ListSelectionListener");
        }
    }

    @Override
    public void onResume(){
        m_ProgressBar.setVisibility(View.VISIBLE);
        new AsyncLoadingTask().execute();
        super.onResume();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_level_select, container, false);

        m_ProgressBar = (ProgressBar)view.findViewById(R.id.progressBar_LevelSelect);

        m_ListView = (ListView)view.findViewById(R.id.listView_levelSelect);

        return view;
    }

    public void RequestSort(SortType sortType){
        m_CurrentSortType = sortType;
        if(m_Levels != null) {
            switch (sortType) {
                case ScoreHI:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_LevelScore() > rhs.getM_LevelScore()) return -1;
                            if (lhs.getM_LevelScore() < rhs.getM_LevelScore()) return 1;
                            return 0;
                        }
                    });
                    break;
                case ScoreLO:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_LevelScore() < rhs.getM_LevelScore()) return -1;
                            if (lhs.getM_LevelScore() > rhs.getM_LevelScore()) return 1;
                            return 0;
                        }
                    });
                    break;
                case RowsHI:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_Rows() > rhs.getM_Rows()) return -1;
                            if (lhs.getM_Rows() < rhs.getM_Rows()) return 1;
                            return 0;
                        }
                    });
                    break;
                case RowsLO:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_Rows() < rhs.getM_Rows()) return -1;
                            if (lhs.getM_Rows() > rhs.getM_Rows()) return 1;
                            return 0;
                        }
                    });
                    break;
                case ColumnsHI:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_Columns() > rhs.getM_Columns()) return -1;
                            if (lhs.getM_Columns() < rhs.getM_Columns()) return 1;
                            return 0;
                        }
                    });
                    break;
                case ColumnsLO:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_Columns() < rhs.getM_Columns()) return -1;
                            if (lhs.getM_Columns() > rhs.getM_Columns()) return 1;
                            return 0;
                        }
                    });
                    break;
                case Name:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (Integer.parseInt(lhs.getM_LevelName()) > Integer.parseInt(rhs.getM_LevelName()))
                                return -1;
                            if (Integer.parseInt(lhs.getM_LevelName()) < Integer.parseInt(rhs.getM_LevelName()))
                                return 1;
                            return 0;
                        }
                    });
                    break;
                case Completed:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_IsComplete() && !rhs.getM_IsComplete()) return -1;
                            if (!lhs.getM_IsComplete() && rhs.getM_IsComplete()) return 1;
                            return 0;
                        }
                    });
                    break;
                case Uncompleted:
                    Collections.sort(m_Levels, new Comparator<LevelObject>() {
                        @Override
                        public int compare(LevelObject lhs, LevelObject rhs) {
                            if (lhs.getM_IsComplete() && !rhs.getM_IsComplete()) return 1;
                            if (!lhs.getM_IsComplete() && rhs.getM_IsComplete()) return -1;
                            return 0;
                        }
                    });
                    break;
                default:
                    break;
            }
            ((LevelListAdapter) m_ListView.getAdapter()).notifyDataSetChanged();
        }
    }

    private class AsyncLoadingTask extends AsyncTask<String,Void,ArrayList<LevelObject>>{
        ArrayList<LevelObject> result = new ArrayList<>();
        @Override
        protected ArrayList<LevelObject> doInBackground(String...params){
                File dir = m_Context.getDir("levels", m_Context.MODE_PRIVATE);

                File[] files = dir.listFiles();
                for (File file: files){
                    result.add(ParseJson(getFileText(file)));
                }
            return result;
        }

        private LevelObject ParseJson(String rawJSON){
            String levelName = "";
            long levelTime = 0l;
            int levelScore = 0,rows = 0, columns = 0;
            Boolean levelComplete = false;

            try {
                JSONObject object = new JSONObject(rawJSON);
                JSONObject puzzleObject = object.getJSONObject("Puzzle");
                levelName = puzzleObject.getString("Id");
                rows = puzzleObject.getInt("Rows");
                columns = puzzleObject.getInt("Columns");
                JSONObject scoreObject;
                if(!object.isNull("PuzzleResult")){ //The level has results stored
                    scoreObject = object.getJSONObject("PuzzleResult");
                    levelTime = scoreObject.getLong("timeRemaining");
                    levelScore = scoreObject.getInt("score");
                    levelComplete = scoreObject.getBoolean("complete");
                }
            }
            catch (JSONException e){e.printStackTrace();}
            return new LevelObject(levelName,levelScore,
                    StringFormatter.FormatTime(levelTime),levelComplete,rows,columns);
        }
        private String getFileText(File file){
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
            return levelJSON;
        }
        @Override
        protected void onPostExecute(ArrayList<LevelObject> result){
            super.onPostExecute(result);
            m_Levels = result;
            LevelListAdapter adapter = new LevelListAdapter(m_Context,m_Levels,m_Listener);
            m_ListView.setAdapter(adapter);
            m_ProgressBar.setVisibility(View.GONE);
            RequestSort(m_CurrentSortType);
        }
    }
}

