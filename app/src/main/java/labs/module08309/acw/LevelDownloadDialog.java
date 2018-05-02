package labs.module08309.acw;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Toby on 16/03/2016.
 */
public class LevelDownloadDialog extends DialogFragment{
    private Context m_Context;
    private String m_URLExtension;
    private ProgressBar m_progressBar;
    private TextView m_textView;
    private DialogStatus m_DialogStatus;

    private static final String DEBUG_TAG = "LevelDownloadFrag";

    public LevelDownloadDialog() {
    }
    public interface DialogStatus{
        void onSuccess();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        m_Context = context;
        try{
            m_DialogStatus = (DialogStatus) context;

        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() +
                    "must implement ListSelectionListener and ProgressListener");
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        Bundle args = getArguments();
        m_URLExtension = args.getString("URLExtension"); //the extension of the JSON file

        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_level_download, container);
        m_progressBar = (ProgressBar)view.findViewById(R.id.progressBar_LevelDownloadDialog);
        m_textView = (TextView)view.findViewById(R.id.text_LevelDownloadDialog);

        if(savedInstanceState == null)
            new AsyncLevelDownload().execute();
        super.onCreate(savedInstanceState);
        return view;
    }
    @Override public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString("URLExtension", m_URLExtension);
        super.onSaveInstanceState(savedInstanceState);
    }



    private class AsyncLevelDownload extends AsyncTask<String,Void,String> {
        String JSONResult;
        InputStream inputStream;

        @Override
        protected void onPreExecute(){
            m_textView.setText(m_Context.getString(R.string.m_TextView_text) + m_URLExtension);
        }

        @Override
        protected String doInBackground(String...params){
            try{
                JSONResult = download();}catch (IOException e) {
                return "Unable to retrieve data from webpage";
            }
            return JSONResult;
        }
        private String download() throws IOException{

            //Setup
            String urlString =  getString(R.string.download_List_URL)+ "puzzles/puzzle" +  m_URLExtension + ".json";
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
                return Reader(inputStream);
                }
                else{
                    Dialog warning = new Dialog(m_Context);
                    warning.setTitle("WebSite unavailable");
                    return "";
                }
            }
            finally { //make sure stream dies
                if(inputStream != null){
                    inputStream.close();
                }
            }
        }
        public String Reader(InputStream stream) throws IOException{
            String result = "";
            String readLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            m_progressBar.setProgress(0);

            while((readLine = br.readLine())!= null){
                /*if the progress bar is not full increase it's progress by one fifth of max
                    if the progress bar fills then reset the progress bar and begin again
                    this isn't accurate but does give the user feedback that the download is actually in progress
                 */
                if(m_progressBar.getProgress() <=m_progressBar.getMax())
                    m_progressBar.setProgress(m_progressBar.getProgress() + m_progressBar.getMax()/5);
                else
                    m_progressBar.setProgress(0);

                result +=readLine;
            }

            m_progressBar.setProgress(m_progressBar.getMax());
            return result;
        }
        @Override
        protected void onPostExecute(String pResult){
            super.onPostExecute(pResult);
            ParseJSON(pResult);
        }
        private void ParseJSON(String result){
            JSONObject object;
            String pictureSet;
            try{
                Log.d(DEBUG_TAG, result);
                object = new JSONObject(result);
                File dir = m_Context.getDir("levels", Context.MODE_PRIVATE);
                File file = new File(dir,"puzzle" + m_URLExtension);

                try {
                    FileOutputStream stream = new FileOutputStream(file);
                    byte[]output = result.getBytes();
                    stream.write(output, 0, output.length);
                }
                catch (Exception e){
                    Toast.makeText(m_Context, "Error saving file", Toast.LENGTH_SHORT).show();}

                JSONObject jsonObject = object.getJSONObject("Puzzle");
                pictureSet = jsonObject.getString("PictureSet");

                File pictureFolder = new File(pictureSet);

                if(!pictureFolder.exists() && !pictureFolder.isDirectory()){
                    new AsyncPictureSetDownload().execute(pictureSet);
                }
                else{
                    m_DialogStatus.onSuccess();
                    getDialog().hide();
                }
            }catch (JSONException e){e.printStackTrace();}
            }
        }
    public class AsyncPictureSetDownload extends AsyncTask<String,Void,String> {
        String JSONResult;
        InputStream inputStream;
        String URLExtension;

        @Override
        protected void onPreExecute(){
            m_textView.setText(m_Context.getString(R.string.m_TextView_dialog_text) + m_URLExtension);
        }

        @Override
        protected String doInBackground(String...params){
            try {
                URLExtension = params[0];
                JSONResult = download();
            }catch (IOException e) {
                return "Unable to retrieve data from webpage";
            }
            return JSONResult;
        }
        private String download() throws IOException{

            //Setup
            String urlString =  getString(R.string.download_List_URL)+ "picturesets/" +  URLExtension;
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
                    return reader(inputStream);
                }
                else{
                    Dialog warning = new Dialog(m_Context);
                    warning.setTitle("WebSite unavailable");
                    return "";
                }
            }
            finally { //make sure stream dies
                if(inputStream != null){
                    inputStream.close();
                }
            }
        }
        public String reader(InputStream stream) throws IOException{
            String result = "";
            String readLine;
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));


            m_progressBar.setProgress(0);

            while((readLine = br.readLine())!= null){
                /*if the progress bar is not full increase it's progress by one fifth of max
                    if the progress bar fills then reset the progress bar and begin again
                    this isn't accurate but does give the user feedback that the download is actually in progress
                 */
                if(m_progressBar.getProgress() <=m_progressBar.getMax())
                    m_progressBar.setProgress(m_progressBar.getProgress() + m_progressBar.getMax()/5);
                else
                    m_progressBar.setProgress(0);

                result +=readLine;
            }

            m_progressBar.setProgress(m_progressBar.getMax());

            return result;
        }
        @Override
        protected void onPostExecute(String pResult){
            super.onPostExecute(pResult);
            String[]pictures = ParseJSON(pResult);
            for(int i = 0; i < pictures.length; i++){
                if(i == pictures.length - 1)
                    new AsyncPictureDownload().execute(pictures[i],"true");
                else
                    new AsyncPictureDownload().execute(pictures[i],"false");
            }
        }

        private String[] ParseJSON(String result){
            JSONObject object;
            String[] pictureSet = null;
            try{
                Log.d(DEBUG_TAG, result);
                object = new JSONObject(result);
                JSONObject jsonObject = object.getJSONObject("PictureSet");
                JSONArray array = jsonObject.optJSONArray("Files");
                pictureSet = new String[array.length()];

                for (int i = 0; i < array.length(); i++){
                    pictureSet[i] = array.getString(i);
                }

                File dir = m_Context.getDir("picturesets", Context.MODE_PRIVATE);
                File file = new File(dir,URLExtension);

                FileOutputStream writer = null;
                try {
                    writer = new FileOutputStream(file);
                    byte[]output = result.getBytes();
                    writer.write(output, 0, output.length);
                }
                catch (Exception e){Toast.makeText(m_Context, "Error saving file", Toast.LENGTH_SHORT).show();}
                finally {
                    try {
                        if(writer != null)
                            writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }catch (JSONException e){e.printStackTrace();}
            return pictureSet;
        }
    }
    private class AsyncPictureDownload extends AsyncTask<String,Void,String> {

        InputStream inputStream;
        String URLExtension;
        boolean isLast = false;

        @Override
        protected void onPreExecute(){
            m_textView.setText("Downloading Images for " + m_URLExtension);
        }

        @Override
        protected String doInBackground(String...params){
            try{
                URLExtension = params[0];
                if(params[1] == "true")
                    isLast = true;
                download();}catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
        private void download() throws IOException{

            //Setup
            String urlString =  getString(R.string.download_List_URL)+ "images/" +  URLExtension;
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
                    reader();
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
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void reader() {

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            File dir = m_Context.getDir("images", Context.MODE_PRIVATE);
            File file = new File(dir, URLExtension);

            m_progressBar.setIndeterminate(true);

            try (FileOutputStream writer = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, writer);
            } catch (Exception e) {
                Toast.makeText(m_Context, "Error downloading images", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(isLast){
                if(getDialog() != null)
                    getDialog().hide();
                m_DialogStatus.onSuccess();
            }
        }
    }

    }

