package com.cookandroid.dailyworkcheck;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class ChecklistAdapter extends BaseAdapter {

    public interface OnItemActionListener {
        void onOpenLink(ChecklistItem item);
        void onEdit(ChecklistItem item);
        void onDelete(ChecklistItem item);
        void onCompletedChanged(ChecklistItem item, boolean completed);
    }

    private Context context;
    private ArrayList<ChecklistItem> items;
    private OnItemActionListener listener;

    public ChecklistAdapter(Context context, ArrayList<ChecklistItem> items, OnItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ChecklistItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_checklist, parent, false);
        }

        ChecklistItem item = items.get(position);

        CheckBox checkCompleted =
                view.findViewById(R.id.checkCompleted);
        TextView textItemTitle =
                view.findViewById(R.id.textItemTitle);
        Button buttonEdit =
                view.findViewById(R.id.buttonEdit);
        Button buttonDelete =
                view.findViewById(R.id.buttonDelete);

        textItemTitle.setText(item.getTitle());

        // 재사용된 체크박스의 이전 이벤트 제거
        checkCompleted.setOnCheckedChangeListener(null);
        checkCompleted.setChecked(item.isCompleted());

        if (item.isCompleted()) {
            textItemTitle.setPaintFlags(
                    textItemTitle.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            textItemTitle.setPaintFlags(
                    textItemTitle.getPaintFlags()
                            & ~Paint.STRIKE_THRU_TEXT_FLAG
            );
        }

        // 리스트 한 줄 터치
        view.setOnClickListener(v -> {
            listener.onOpenLink(item);
        });

        checkCompleted.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    listener.onCompletedChanged(item, isChecked);
                }
        );

        buttonEdit.setOnClickListener(v -> {
            listener.onEdit(item);
        });

        buttonDelete.setOnClickListener(v -> {
            listener.onDelete(item);
        });

        return view;
    }
}