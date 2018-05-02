package labs.module08309.acw;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Toby on 16/03/2016.
 */
public class LevelDownloadActivity extends AppCompatActivity implements LevelListDownloadFragment.ListSelectionListener, LevelDownloadDialog.DialogStatus{


    private static final String DEBUG_TAG = "LevelDownloadActivity";
    private LevelListDownloadFragment m_LevelListDownloadFragment = null;
    private LevelObject m_Selected;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_download);
        if(savedInstanceState == null){
            //TODO pass List of Already downloaded levels to fragment
            m_LevelListDownloadFragment = new LevelListDownloadFragment();
            getFragmentManager().beginTransaction().add(R.id.layout_levelDownloadActivity, m_LevelListDownloadFragment).commit();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        PairApplication.onGainedFocus();
    }

    @Override
    public void onSuccess(){
        m_LevelListDownloadFragment.RemoveListItem(m_Selected);
    }

    @Override
    public void onListSelection(LevelObject selected){
        m_Selected = selected;
        FragmentManager fragmentManager = getFragmentManager();

        LevelDownloadDialog levelDownloadDialog = new LevelDownloadDialog();
        Bundle b = new Bundle();
        b.putString("URLExtension", selected.getM_LevelName());
        levelDownloadDialog.setArguments(b);
        levelDownloadDialog.show(fragmentManager,"dialog_levelDownload");
    }

    @Override
    protected void onPause() {
        super.onPause();
        PairApplication.onLostFocus();
    }
}
