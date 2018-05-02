package labs.module08309.acw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Toby on 11/03/2016.
 */
public class LevelSelectActivity extends AppCompatActivity implements LevelSelectFragment.ListSelectionListener {
    private static final int PICK_NEXT_ACTIVITY = 0;
    private LevelSelectFragment m_LevelSelectFragment;
    private String[] m_spinnerArrayValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_LevelSelect_Sort);

        m_spinnerArrayValues = getResources().getStringArray(R.array.sort_spinner_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.
                simple_spinner_item, m_spinnerArrayValues);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    /*
                            ScoreHI,
                            ScoreLO,
                            RowsHI,
                            RowsLO,
                            ColumnsHI,
                            ColumnsLO,
                            Name,
                            Completed,
                            Uncompleted
                     */
                    case 0:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.ScoreHI);
                        break;
                    case 1:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.ScoreLO);
                        break;
                    case 2:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.RowsHI);
                        break;
                    case 3:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.RowsLO);
                        break;
                    case 4:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.ColumnsHI);
                        break;
                    case 5:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.ColumnsLO);
                        break;
                    case 6:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.Name);
                        break;
                    case 7:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.Completed);
                        break;
                    case 8:
                        m_LevelSelectFragment.RequestSort(LevelSelectFragment.SortType.Uncompleted);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //TODO:Set list adapter to be location to all of the levels

        if (savedInstanceState == null) {
            m_LevelSelectFragment = new LevelSelectFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.layout_LevelSelect, m_LevelSelectFragment)
                    .commit();
        }
        else
            m_LevelSelectFragment =(LevelSelectFragment) getFragmentManager()
                    .getFragment(savedInstanceState, "m_LevelSelectFragment");
    }
    @Override
    public void onListSelection(String selected){
        Intent intent = new Intent(this,LevelSimpleActivity.class);
        intent.putExtra("level", selected);
        startActivityForResult(intent, PICK_NEXT_ACTIVITY);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            getFragmentManager().putFragment(savedInstanceState, "m_LevelSelectFragment", m_LevelSelectFragment);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        /*
            A switch is overkill but I don't see any harm in allowing room for scaling
         */
        switch (requestCode){
            case PICK_NEXT_ACTIVITY:
                if(resultCode == RESULT_CANCELED) //User wants to return to main menu
                    finish();
                //If the user returned result OK then we're already on the right screen
                break;
            default:
                new Exception("Error on return activity result").printStackTrace();
                break;
        }
    }
    public void levelDownloadOnClick(View view){
        Intent intent = new Intent(this,LevelDownloadActivity.class);
        startActivity(intent);
    }
}
