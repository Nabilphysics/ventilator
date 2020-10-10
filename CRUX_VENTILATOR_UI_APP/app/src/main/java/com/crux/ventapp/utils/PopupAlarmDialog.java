package com.crux.ventapp.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;

import com.crux.ventapp.R;

public class PopupAlarmDialog {

    AlertDialog.Builder builder;
    AlertDialog dialog;
    private OnButtonClicked onButtonClicked;
    Context context;

    public PopupAlarmDialog(Context context, int defaultValue) {
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_time, null,false);
        NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(60);
        numberPicker.setMinValue(1);
        numberPicker.setValue(defaultValue);

        builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_MaterialComponents_Dialog))
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("Close", (dialog, which) -> {
                    onButtonClicked.onNegativeButtonClicked(dialog);
                })
                .setPositiveButton("Save", (dialog, which) -> {
                    onButtonClicked.onPositiveButtonClicked(dialog,numberPicker.getValue());
                });
        dialog = builder.create();
    }

    public void setDialogTitle(String title){
        builder.setTitle(title);
    }

    public void showPopupAlarmAdjustDialog(){
        dialog.show();
    }

    public interface OnButtonClicked {
        void onPositiveButtonClicked(DialogInterface dialog,int value);
        void onNegativeButtonClicked(DialogInterface dialog);
    }

    public void setOnButtonClicked(OnButtonClicked onButtonClicked) {
        this.onButtonClicked = onButtonClicked;
    }

}
