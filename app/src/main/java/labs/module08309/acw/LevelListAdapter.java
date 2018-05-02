package labs.module08309.acw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * Created by Toby on 23/04/2016.
 */
class LevelListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<LevelObject> storage;
    private LevelSelectFragment.ListSelectionListener listener;

    public LevelListAdapter(Context context, ArrayList<LevelObject> list ,LevelSelectFragment.ListSelectionListener listener){
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        storage = list;
        this.listener = listener;
    }

    @Override

    public int getCount() {

        return storage.size();

    }

    @Override

    public Object getItem(int position) {

        return storage.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;

        if(convertView == null){

            listViewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.listview_adapter_level_select, parent, false);

            listViewHolder.levelName = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_label_Title);

            listViewHolder.levelScore = (TextView)convertView.findViewById(R.id.text_listviewAdapter_Score);

            listViewHolder.levelTime = (TextView)convertView.findViewById(R.id.text_listviewAdapter_Time);

            listViewHolder.levelComplete = (CheckBox)convertView.findViewById(R.id.checkBox_listviewAdapter_Completed);

            listViewHolder.rows = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_Rows);

            listViewHolder.columns = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_Columns);

            convertView.setTag(listViewHolder);

        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListSelection("puzzle" + storage.get(position).getM_LevelName());
            }
        });

        listViewHolder.levelName.setText(storage.get(position).getM_LevelName());
        listViewHolder.levelTime.setText(storage.get(position).getM_LevelTime());
        listViewHolder.levelScore.setText(String.valueOf(storage.get(position).getM_LevelScore()));
        listViewHolder.levelComplete.setChecked(storage.get(position).getM_IsComplete());
        listViewHolder.rows.setText(String.valueOf(storage.get(position).getM_Rows()));
        listViewHolder.columns.setText(String.valueOf(storage.get(position).getM_Columns()));
        return convertView;
    }
    static class ViewHolder{
        TextView levelName;
        TextView levelScore;
        TextView levelTime;
        TextView rows;
        TextView columns;
        CheckBox levelComplete;
    }
}
