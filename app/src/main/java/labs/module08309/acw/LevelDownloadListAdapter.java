package labs.module08309.acw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Toby on 23/04/2016.
 */
class LevelDownloadListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private ArrayList<LevelObject> storage;
    private LevelListDownloadFragment.ListSelectionListener listener;

    public LevelDownloadListAdapter(Context context, ArrayList<LevelObject> list,  LevelListDownloadFragment.ListSelectionListener listener){
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        storage = list;
        this.listener = listener;
    }

    @Override

    public int getCount() {

        return storage.size();

    }
    public void remove(LevelObject object){
        storage.remove(object);
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

            convertView = inflater.inflate(R.layout.listview_adapter_level_download, parent, false);

            listViewHolder.levelName = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_label_Title);

            listViewHolder.rows = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_Rows);

            listViewHolder.columns = (TextView)convertView.findViewById(R.id.text_listviewDownloadAdapter_Columns);

            convertView.setTag(listViewHolder);

        }else{
            listViewHolder = (ViewHolder)convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onListSelection(storage.get(position));
            }
        });

        listViewHolder.levelName.setText(storage.get(position).getM_LevelName());
        listViewHolder.rows.setText(String.valueOf(storage.get(position).getM_Rows()));
        listViewHolder.columns.setText(String.valueOf(storage.get(position).getM_Columns()));
        return convertView;
    }
    static class ViewHolder{
        TextView levelName;
        TextView rows;
        TextView columns;
    }
}
