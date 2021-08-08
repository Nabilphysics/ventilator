package com.crux.ventapp.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;

import com.crux.ventapp.R;

public class CustomDialog {

    Context context;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    public OnButtonClicked onButtonClicked;

    public CustomDialog(Context context) {
        this.context = context;
    }

    public void createDialog(String title, String message, String negativeButtonText, String positiveButtonText) {
        builder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_MaterialComponents_Dialog))
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(negativeButtonText, (dialog, which) -> {
                    onButtonClicked.onNegativeButtonClicked(dialog);
                })
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    onButtonClicked.onPositiveButtonClicked(dialog);
                });

        dialog = builder.create();

    }

    public void setIcon(int iconId) {
        dialog.setIcon(iconId);
    }

    public void showDialog() {
        dialog.show();
    }

    public void hideDialog() {
        dialog.dismiss();
    }

    public interface OnButtonClicked {
        void onPositiveButtonClicked(DialogInterface dialog);

        void onNegativeButtonClicked(DialogInterface dialog);
    }

    public void setOnButtonClicked(OnButtonClicked onButtonClicked) {
        this.onButtonClicked = onButtonClicked;
    }
}
