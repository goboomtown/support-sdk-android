package com.goboomtown.supportsdk.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;

/**
 * Created by larry on 2016-04-17.
 */
public class CustomGridAdapter extends BaseAdapter {
    private ArrayList<View> mButtons;

    public CustomGridAdapter(Context context, ArrayList<View> buttons) {
        mButtons = buttons;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mButtons.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
//        View grid;
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        if (convertView == null) {
//            grid = inflater.inflate(R.layout.grid_item, null);
//        } else {
//            grid = convertView;
//        }
//        SupportMenuButton button = grid.findViewById(R.id.button);
//        mButtons.get(position);

//        SupportMenuButton button = mButtons.get(position);
//        ImageButton imageButton = grid.findViewById(R.id.imageButton);
//        imageButton.setImageDrawable(button.mImageButton.getDrawable());
//
//        TextView labelView = grid.findViewById(R.id.labelView);
//        labelView.setText(button.mLabelView.getText());

//        return grid;
        return mButtons.get(position);
    }
}

