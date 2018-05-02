package labs.module08309.acw;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Toby on 16/03/2016.
 */
public class LevelListDownloadFragment extends Fragment{

    private final String URLEXTENSION = "index.json";
    private ListView m_DownloadList;
    private LevelDownloadListAdapter m_Adapter;
    private LinearLayout m_LinearLayout;
    private ProgressBar m_ProgressBar;
    private Context m_Context;
    private ArrayList<LevelObject> m_Levels;
    private ArrayList<String> m_LevelNames;

    private static final String DEBUG_TAG = "LevelListDownloadFrag";

    private ListSelectionListener m_ListListener = null;

    public interface ListSelectionListener{
        void onListSelection(LevelObject selected);
    }

    public void RemoveListItem(LevelObject item){
       m_Adapter.remove(item);
        m_DownloadList.setAdapter(m_Adapter);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        m_Context = context;
        try{
            m_ListListener = (ListSelectionListener) context;

        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    "must implement ListSelectionListener and ProgressListener");
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        if(savedInstanceState == null){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                    new AsyncLevelListDownload().execute();
                }
            }, 1000L); // delay timer
        }
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_download, container, false);
        m_LinearLayout = (LinearLayout)view.findViewById(R.id.layout_levelDownload);
        m_ProgressBar = (ProgressBar)view.findViewById(R.id.progressBar_LevelDownload);
        m_DownloadList = (ListView)view.findViewById(R.id.list_AvailableDownloads);
        m_Levels = new ArrayList<>();

        if(savedInstanceState != null){
            m_LinearLayout.removeView(m_ProgressBar);
            m_Levels =(ArrayList<LevelObject>) savedInstanceState.getSerializable("levels");
            m_Adapter = new LevelDownloadListAdapter(m_Context,m_Levels,m_ListListener);
            m_DownloadList.setAdapter(m_Adapter);
        }
        super.onCreate(savedInstanceState);
        return view;
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putSerializable("levels", m_Levels);
        super.onSaveInstanceState(savedInstanceState);
    }

    private class AsyncLevelListDownload extends AsyncTask<String,Void,String> {
        String m_JSONResult;
        InputStream m_iStream;

        @Override
        protected String doInBackground(String...params){
            try{
                m_JSONResult = download();}catch (IOException e) {
                return "Unable to retireve data from webpage";
            }
            /*
                This section checks for files that are already downloaded and excludes them from the
                list of levels to download
             */
            ParseJSON(m_JSONResult);
            return m_JSONResult;
        }
        private String download() throws IOException{

            //Setup
            String urlString =  getString(R.string.download_List_URL) + URLEXTENSION;
            URL url;
            try{
                url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                //Query
                connection.connect();
                int response = connection.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                m_iStream = connection.getInputStream();

                //Parse
                return reader(m_iStream);
            }
            finally { //make sure stream dies
                if(m_iStream != null){
                    m_iStream.close();
                }
            }
        }
        public String reader(InputStream stream) throws IOException{
            String result = "";
            String readLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));


            while((readLine = br.readLine())!= null){
                result +=readLine;
            }
            return result;
        }
        @Override
        protected void onPostExecute(String pResult){
            m_LinearLayout.removeView(m_ProgressBar);
            super.onPostExecute(pResult);
            m_Adapter = new LevelDownloadListAdapter(m_Context,m_Levels,m_ListListener);
            m_DownloadList.setAdapter(m_Adapter);
        }
        private void ParseJSON(String result){
            JSONObject object;
            JSONArray JSONArray = null;
            m_LevelNames = new ArrayList<>();
            try{
                Log.d(DEBUG_TAG, result);
                object = new JSONObject(result);
                JSONArray = object.getJSONArray("PuzzleIndex");
            }catch (JSONException e){e.printStackTrace();}
            assert JSONArray != null;
            for (int i = 0; i < JSONArray.length(); i++){
                try{
                    Log.d(DEBUG_TAG, ("\"" + JSONArray.getString(i) + "\""));
                    String dirtyResult = JSONArray.getString(i);
                    m_LevelNames.add(dirtyResult);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
            File dir = m_Context.getDir("levels",m_Context.MODE_PRIVATE);
            File file;
            ArrayList<String> toDelete = new ArrayList<>();

            //Add all duplicate levels to delete buffer
            for (int i = 0; i < m_LevelNames.size(); i++){
                String levelNameDirty = m_LevelNames.get(i);
                String levelName = levelNameDirty.substring(0,levelNameDirty.lastIndexOf("."));
                file = new File(dir,levelName);
                if(file.exists())
                    toDelete.add(m_LevelNames.get(i));
            }
            //delete all levels that are duplicated
            for (int i = 0; i < toDelete.size(); i++){
                m_LevelNames.remove(m_LevelNames.indexOf(toDelete.get(i)));
            }

            for (String level: m_LevelNames) {
                try {
                    downloadLevel(level);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void downloadLevel(String level) throws IOException{
            InputStream inputStream = null;
            //Setup
            String urlString =  getString(R.string.download_List_URL)+ "puzzles/" +  level;
            URL url;
            try{
                url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                //Query
                connection.connect();
                int response = connection.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                if (response == 200){
                    //Get Connection
                    inputStream = connection.getInputStream();
                    //Parse
                   m_Levels.add(ParseLevelJSON(LevelReader(inputStream)));
                }
                else{
                    Dialog warning = new Dialog(m_Context);
                    warning.setTitle("WebSite unavailable");
                }
            }
            finally { //make sure stream dies
                if(inputStream != null){
                    inputStream.close();
                }
            }
        }
        public String LevelReader(InputStream stream) throws IOException{
            String result = "";
            String readLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            while((readLine = br.readLine())!= null){
                result +=readLine;
            }
            return result;
        }
        private LevelObject ParseLevelJSON(String result){

            String levelName = "";
            int columns =0;
            int rows = 0;

            try{
                Log.d(DEBUG_TAG, result);
                JSONObject object = new JSONObject(result);
                JSONObject levelObject = object.getJSONObject("Puzzle");
                levelName = levelObject.getString("Id");
                columns = levelObject.getInt("Columns");
                rows = levelObject.getInt("Rows");
            }catch (JSONException e){e.printStackTrace();}
            return new LevelObject(levelName,0,"0:00",false,rows,columns);
        }
    }
}

