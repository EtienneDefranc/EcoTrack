package ec.com.ecotrackapp.tda;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ec.com.ecotrackapp.tda.List;

public class TdaListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> items;

    public TdaListAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @Override public int getCount() { return items.size(); }

    @Override public Object getItem(int position) { return items.get(position); }

    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = (convertView instanceof TextView)
                ? (TextView) convertView
                : (TextView) View.inflate(context, android.R.layout.simple_list_item_1, null);

        tv.setText(items.get(position));
        return tv;
    }
}
