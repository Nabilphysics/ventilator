package com.crux.ventapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.anychart.AnyChartView;
import com.crux.ventapp.utils.AlarmController;
import com.crux.ventapp.utils.Constants;
import com.crux.ventapp.utils.CustomDialog;
import com.crux.ventapp.utils.Helper;
import com.crux.ventapp.utils.MeasuredHelper;
import com.crux.ventapp.utils.PopupAlarmDialog;
import com.crux.ventapp.utils.SoundPlayer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import static com.crux.ventapp.DeviceListActivity.btSocket;

public class GraphActivity extends AppCompatActivity {

    private static final String TAG = "GraphActivity";
    LineChart vtGraph, flowGraph, pressureGraph;
    AppCompatTextView tvVtValue, tvFlowValue, tvPressureValue, tvPeepValue, tvBpmValue, tvPipValue, tvOxygenValue,
            tvTitleInsertValue, tvPeakPressureValue, tvBpmInputValue, tvTiInputValue,
            tvPeepInputValue, tvPipInputValue, tvTvInputValue, tvDebug, tvConnectionText, tvTvAlertInputHigh, tvTvAlertInputLow,
            tvPipAlertInputHigh, tvPipAlertInputLow, tvPeepAlertInputHigh, tvPeepAlertInputLow, tvCurrentModeText, tvTvAlarmValueDisplayHigh,
            tvTvAlarmValueDisplayLow, tvPipAlarmValueDisplayHigh, tvPipAlarmValueDisplayLow, tvPeepAlarmValueDisplayHigh, tvPeepAlarmValueDisplayLow, tvTiValueShow, tiSeekBarInputValue, tvIERatio, tvAlertDialogTitle, tvAlertDialogMessage, peepAlertDialogTitle,
            peepAlertDialogMessage, pipAlertDialogTitle, pipAlertDialogMessage, tvLowAlarmDelayValue, tvHighAlarmDelayValue;

    RelativeLayout bottomSheetMode, modeInputContainer, settingsInputContainer, alarmInputContainer;
    LinearLayoutCompat bottomSheetInput, bottomSheetAlarm, tvAlertInputHighContainer,
            tvAlertInputLowContainer, ieRatioContainer, autoTiContainer, pipAlertInputHighContainer, pipAlertInputLowContainer,
            peepAlertInputHighContainer, peepAlertInputLowContainer;

    NestedScrollView bottomSheetSettings;
    BottomSheetBehavior bottomSheetBehaviorInput, bottomSheetBehaviorModes, bottomSheetBehaviorSettings,
            bottomSheetBehaviorAlarm;


    AppCompatButton btnUpdateValue, btAlertUpdate, btChangeLowAlarmDelay, btChangeHighAlarmDelay;
    AppCompatEditText etInputValue, etAlertInputValue, etTiInputValue;


    AnyChartView anyChartView;
    BarChart bcPressure;

    MaterialCardView bpmInputContainer, tiiInputContainer, peepInputContainer, pipInputContainer, tvInputContainer,
            pressureSupportMode, prvcMode, cpapMode, simvMode, acMode, highTvAlertDialog, lowTvAlertDialog, highPeepAlertDialog,
            lowPeepAlertDialog, highPipAlertDialog, lowPipAlertDialog;

    SwitchMaterial switchOnOff, switchSnoozeHighTvAlarm, switchSnoozeLowTvAlarm, switchSnoozeHighPeepAlarm,
            switchSnoozeLowPeepAlarm, switchSnoozeHighPipAlarm, switchSnoozeLowPipAlarm, switchDebugText;

    RangeSeekBar seekBarInputValue, seekBarTiInputValue, seekBarAlertInputValue;
    ;

    AppCompatButton btCloseHighTvAlertDialog, btSnoozeHighTvAlertDialog, btCloseLowTvAlertDialog, btSnoozeLowTvAlertDialog, btCloseHighPeepAlertDialog, btSnoozeHighPeepAlertDialog, btCloseLowPeepAlertDialog,
            btSnoozeLowPeepAlertDialog, btCloseHighPipAlertDialog, btSnoozeHighPiPAlertDialog, btCloseLowPipAlertDialog, btSnoozeLowPiPAlertDialog;

    String selectedInputButton = Constants.BTN_TV;
    String selectedAlertInput;
    String activatedMode = Constants.MODE_PRVC_ACTIVATED;
    String clickedMode;
    float peakPressure = 0;

    static String mode = "v", bpm = "10", tv = "150", ti = "2.0", peep = "05", pip = "10", tvAlarmHigh = "100", tvAlarmLow =
            "50", pipAlarmHigh = "40", pipAlarmLow = "0", peepAlarmHigh = "40", peepAlarmLow = "0";

    static int lowAlarmDelayValue = 1;
    static int highAlarmDelayValue = 1;

    CustomDialog customDialog;

    AppCompatImageView ivPressureSupportIcon, ivPrvcModeIcon, ivCpapModeIcon, ivSimvModeIcon, ivAcModeIcon,
            ivConnectionStatus;

    float seekStep = 1;
    float seekMax = 100;
    float seekMin = 0;

    Helper helper = new Helper();

    SoundPlayer soundPlayer;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker = false;
    volatile boolean isVentilatorOn = false;
    volatile boolean isAlarmSnoozed = false;
    volatile boolean isHighTvAlarmSnoozed = false;
    volatile boolean isLowTvAlarmSnoozed = false;
    volatile boolean isHighPeepAlarmSnoozed = false;
    volatile boolean isLowPeepAlarmSnoozed = false;
    volatile boolean isHighPipAlarmSnoozed = false;
    volatile boolean isLowPipAlarmSnoozed = false;
    volatile boolean shouldLowAlarmActivate = false;
    volatile boolean shouldHighAlarmActivate = true;


    InputStream mmInputStream;
    Random random;
    boolean isVentilationStarted;
    AlarmController alarmController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_graph);
        vtGraph = findViewById(R.id.vtGraph);
        flowGraph = findViewById(R.id.flowGraph);
        pressureGraph = findViewById(R.id.pressureGraph);

        soundPlayer = new SoundPlayer(this);
        soundPlayer.setAlarmSoundClip(R.raw.vent_alarm_1);


        prepareGraph(vtGraph);
        prepareGraph(flowGraph);
        prepareGraph(pressureGraph);

        tvVtValue = findViewById(R.id.tvVtValue);
        tvFlowValue = findViewById(R.id.tvFlowValue);
        // tvPressureValue = findViewById(R.id.tvPressureValue);
        tvPeepValue = findViewById(R.id.tvPeepValue);
        tvBpmValue = findViewById(R.id.tvBpmValue);
        tvPipValue = findViewById(R.id.tvPipValue);
        tvOxygenValue = findViewById(R.id.tvOxygenValue);
        tvPeakPressureValue = findViewById(R.id.tvPeakPressureValue);
        tvDebug = findViewById(R.id.tvDebug);

        tvTitleInsertValue = findViewById(R.id.tvTitleInsertValue);
        btnUpdateValue = findViewById(R.id.btnUpdateValue);
        btAlertUpdate = findViewById(R.id.btAlertUpdate);
        etInputValue = findViewById(R.id.etInputValue);
        etAlertInputValue = findViewById(R.id.etAlertInputValue);
        seekBarInputValue = findViewById(R.id.seekBarInputValue);
        seekBarTiInputValue = findViewById(R.id.seekBarTiInputValue);
        seekBarAlertInputValue = findViewById(R.id.seekBarAlertInputValue);

        bottomSheetInput = findViewById(R.id.bottomSheetInput);
        bottomSheetBehaviorInput = BottomSheetBehavior.from(bottomSheetInput);
        bottomSheetBehaviorInput.setHideable(true);
        bottomSheetBehaviorInput.setPeekHeight(0, true);

        bottomSheetMode = findViewById(R.id.bottomSheetMode);
        bottomSheetBehaviorModes = BottomSheetBehavior.from(bottomSheetMode);
        bottomSheetBehaviorModes.setHideable(true);
        bottomSheetBehaviorModes.setPeekHeight(0, true);

        bottomSheetSettings = findViewById(R.id.bottomSheetSettings);
        bottomSheetBehaviorSettings = BottomSheetBehavior.from(bottomSheetSettings);
        bottomSheetBehaviorSettings.setHideable(true);
        bottomSheetBehaviorSettings.setPeekHeight(0, true);

        bottomSheetAlarm = findViewById(R.id.bottomSheetAlarm);
        bottomSheetBehaviorAlarm = BottomSheetBehavior.from(bottomSheetAlarm);
        bottomSheetBehaviorAlarm.setHideable(true);
        bottomSheetBehaviorAlarm.setPeekHeight(0, true);


        bpmInputContainer = findViewById(R.id.bpmInputContainer);
        tiiInputContainer = findViewById(R.id.tiInputContainer);
        peepInputContainer = findViewById(R.id.peepInputContainer);
        pipInputContainer = findViewById(R.id.pipInputContainer);
        tvInputContainer = findViewById(R.id.tvInputContainer);

        tvBpmInputValue = findViewById(R.id.tvBpmInputValue);
        tvTiInputValue = findViewById(R.id.tvTiInputValue);
        // tvPressureInputValue = findViewById(R.id.tvPressureInputValue);
        tvPeepInputValue = findViewById(R.id.tvPeepInputValue);
        tvPipInputValue = findViewById(R.id.tvPipInputValue);
        tvTvInputValue = findViewById(R.id.tvVtInputValue);


        modeInputContainer = findViewById(R.id.modeInputContainer);
        settingsInputContainer = findViewById(R.id.settingsInputContainer);
        alarmInputContainer = findViewById(R.id.alarmInputContainer);
        switchOnOff = findViewById(R.id.switchOnOff);
        switchSnoozeHighTvAlarm = findViewById(R.id.switchSnoozeHighTvAlarm);
        switchSnoozeLowTvAlarm = findViewById(R.id.switchSnoozeLowTvAlarm);
        switchSnoozeHighPeepAlarm = findViewById(R.id.switchSnoozeHighPeepAlarm);
        switchSnoozeLowPeepAlarm = findViewById(R.id.switchSnoozeLowPeepAlarm);
        switchSnoozeHighPipAlarm = findViewById(R.id.switchSnoozeHighPipAlarm);
        switchSnoozeLowPipAlarm = findViewById(R.id.switchSnoozeLowPipAlarm);

        switchDebugText = findViewById(R.id.switchDebugText);
        // switchOnOff.setEnabled(false);

        bcPressure = findViewById(R.id.bcPressure);
        bcPressure.getLegend().setEnabled(false);
        bcPressure.getXAxis().setDrawLabels(false);
        bcPressure.getAxisRight().setDrawLabels(false);

        pressureSupportMode = findViewById(R.id.pressureSupportMode);
        prvcMode = findViewById(R.id.prvcMode);
        cpapMode = findViewById(R.id.cpapMode);
        simvMode = findViewById(R.id.simvMode);
        acMode = findViewById(R.id.acMode);

        ivPressureSupportIcon = findViewById(R.id.ivPressureSupport);
        ivPrvcModeIcon = findViewById(R.id.ivPrvcModeIcon);
        ivCpapModeIcon = findViewById(R.id.ivCpapModeIcon);
        ivSimvModeIcon = findViewById(R.id.ivSimvModeIcon);
        ivAcModeIcon = findViewById(R.id.ivAcModeIcon);

        ivConnectionStatus = findViewById(R.id.ivConnectionStatus);
        tvConnectionText = findViewById(R.id.tvConnectionText);

        tvAlertInputHighContainer = findViewById(R.id.tvAlertInputHighContainer);
        tvAlertInputLowContainer = findViewById(R.id.tvAlertInputLowContainer);
        pipAlertInputHighContainer = findViewById(R.id.pipAlertInputHighContainer);
        pipAlertInputLowContainer = findViewById(R.id.pipAlertInputLowContainer);
        peepAlertInputHighContainer = findViewById(R.id.peepAlertInputHighContainer);
        peepAlertInputLowContainer = findViewById(R.id.peepAlertInputLowContainer);

        tvTvAlertInputHigh = findViewById(R.id.tvTvAlertInputHigh);
        tvTvAlertInputLow = findViewById(R.id.tvTvAlertInputLow);
        tvPipAlertInputHigh = findViewById(R.id.tvPipAlertInputHigh);
        tvPipAlertInputLow = findViewById(R.id.tvPipAlertInputLow);
        tvPeepAlertInputHigh = findViewById(R.id.tvPeepAlertInputHigh);
        tvPeepAlertInputLow = findViewById(R.id.tvPeepAlertInputLow);
        tvCurrentModeText = findViewById(R.id.tvCurrentModeText);

        tvTvAlarmValueDisplayHigh = findViewById(R.id.tvTvAlarmValueDisplayHigh);
        tvTvAlarmValueDisplayLow = findViewById(R.id.tvTvAlarmValueDisplayLow);
        tvPipAlarmValueDisplayHigh = findViewById(R.id.tvPipAlarmValueDisplayHigh);
        tvPipAlarmValueDisplayLow = findViewById(R.id.tvPipAlarmValueDisplayLow);
        tvPeepAlarmValueDisplayHigh = findViewById(R.id.tvPeepAlarmValueDisplayHigh);
        tvPeepAlarmValueDisplayLow = findViewById(R.id.tvPeepAlarmValueDisplayLow);


        etTiInputValue = findViewById(R.id.etTiInputValue);
        tvIERatio = findViewById(R.id.tvIERatio);

        ieRatioContainer = findViewById(R.id.ieRatioContainer);
        autoTiContainer = findViewById(R.id.autoTiContainer);


        highTvAlertDialog = findViewById(R.id.highTvAlertDialog);
        btCloseHighTvAlertDialog = findViewById(R.id.btCloseHighTvAlertDialog);
        btSnoozeHighTvAlertDialog = findViewById(R.id.btSnoozeHighTvAlertDialog);

        lowTvAlertDialog = findViewById(R.id.lowTvAlertDialog);
        btCloseLowTvAlertDialog = findViewById(R.id.btCloseLowTvAlertDialog);
        btSnoozeLowTvAlertDialog = findViewById(R.id.btSnoozeLowTvAlertDialog);


        highPeepAlertDialog = findViewById(R.id.highPeepAlertDialog);
        btCloseHighPeepAlertDialog = findViewById(R.id.btCloseHighPeepAlertDialog);
        btSnoozeHighPeepAlertDialog = findViewById(R.id.btSnoozeHighPeepAlertDialog);

        lowPeepAlertDialog = findViewById(R.id.lowPeepAlertDialog);
        btCloseLowPeepAlertDialog = findViewById(R.id.btCloseLowPeepAlertDialog);
        btSnoozeLowPeepAlertDialog = findViewById(R.id.btSnoozeLowPeepAlertDialog);


        highPipAlertDialog = findViewById(R.id.highPipAlertDialog);
        btCloseHighPipAlertDialog = findViewById(R.id.btCloseHighPipAlertDialog);
        btSnoozeHighPiPAlertDialog = findViewById(R.id.btSnoozeHighPiAlertDialog);

        lowPipAlertDialog = findViewById(R.id.lowPipAlertDialog);
        btCloseLowPipAlertDialog = findViewById(R.id.btCloseLowPipAlertDialog);
        btSnoozeLowPiPAlertDialog = findViewById(R.id.btSnoozeLowPiAlertDialog);

        tvLowAlarmDelayValue = findViewById(R.id.tvLowAlarmDelayValue);
        btChangeLowAlarmDelay = findViewById(R.id.btChangeLowAlarmDelay);
        tvHighAlarmDelayValue = findViewById(R.id.tvHighAlarmDelayValue);
        btChangeHighAlarmDelay = findViewById(R.id.btChangeHighAlarmDelay);


        Helper.setupPressureGraph(bcPressure);
        // soundPlayer = new SoundPlayer(this);

        LineData data = new LineData();
        LineData data1 = new LineData();
        LineData data2 = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        vtGraph.setData(data);
        flowGraph.setData(data1);
        pressureGraph.setData(data2);

        setInitialInputValueInViews();
        beginListenForData();


        switchOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // stopWorker = !isChecked;
            isVentilatorOn = isChecked;
            if (isVentilatorOn()) {
                writeToBtModule(combineInputData());
                setLowAlarmSchedule(lowAlarmDelayValue);
            }

            if (!isChecked) {
                customDialog = new CustomDialog(this);
                customDialog.createDialog("Caution !!!", "Turning ventilator off may cause unexpected condition to" +
                        " the patient. Are you sure you want to turn off the ventilator ?", "No", "Yes");
                customDialog.setIcon(R.drawable.ic_action_warning);
                customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                    @Override
                    public void onPositiveButtonClicked(DialogInterface dialog) {
                        writeToBtModule("s");
                        Toast.makeText(GraphActivity.this, "Ventilator turned off", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeButtonClicked(DialogInterface dialog) {
                        switchOnOff.setChecked(true);
                        dialog.dismiss();
                    }
                });
                customDialog.showDialog();
            }
        });

        switchDebugText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvDebug.setVisibility(View.VISIBLE);
            } else {
                tvDebug.setVisibility(View.GONE);
            }
        });

        // region input listening
        bpmInputContainer.setOnClickListener(v -> {
            resetInput();
            selectedInputButton = Constants.BTN_BPM;
            tvTitleInsertValue.setText(R.string.insert_bpm_value);
            // setSeekBarMinMaxStep(6, 40, 1);
            seekBarInputValue.setRange(6, 40, 1);
            // seekBarInputValue.setProgress(0);
            // seekBarInputValue.setMax((int) ((seekMax - seekMin) / seekStep));
            seekBarInputValue.setProgress(Integer.parseInt(tvBpmInputValue.getText().toString()));
            etInputValue.setText(tvBpmInputValue.getText());
            setEnableTiAdjustView(true);
            toggleBottomSheetBehaviour(bottomSheetBehaviorInput);

        });


        tiiInputContainer.setOnClickListener(v -> {

            resetInput();
            selectedInputButton = Constants.BTN_TI;
            tvTitleInsertValue.setText(R.string.insert_ti_value);
            float currentBPM = Float.parseFloat(tvBpmInputValue.getText().toString());
            float maxTi = (((60 / currentBPM) / 3) * 2);
            seekBarInputValue.setRange(0.5f, maxTi);
            seekBarInputValue.setProgress(Float.parseFloat(tvTiInputValue.getText().toString()));
            etInputValue.setText(tvTiInputValue.getText());
            setEnableTiAdjustView(false);
            toggleBottomSheetBehaviour(bottomSheetBehaviorInput);
        });
        peepInputContainer.setOnClickListener(v -> {

            resetInput();
            selectedInputButton = Constants.BTN_PEEP;
            tvTitleInsertValue.setText(R.string.insert_peep_value);
            // setSeekBarMinMaxStep(0.0f, 12f, 1f);
            // seekBarInputValue.setMax((int) ((seekMax - seekMin) / seekStep));
            seekBarInputValue.setRange(0, 12, 1);
            seekBarInputValue.setProgress(Float.parseFloat(tvPeepInputValue.getText().toString()));
            etInputValue.setText(tvPeepInputValue.getText());
            setEnableTiAdjustView(false);
            toggleBottomSheetBehaviour(bottomSheetBehaviorInput);
        });
        pipInputContainer.setOnClickListener(v -> {

            resetInput();
            selectedInputButton = Constants.BTN_PIP;
            tvTitleInsertValue.setText(R.string.insert_pip_value);
            seekBarInputValue.setRange(0, 35, 1);
            seekBarInputValue.setProgress(Float.parseFloat(tvPipInputValue.getText().toString()));
            etInputValue.setText(tvPipInputValue.getText());
            setEnableTiAdjustView(false);
            toggleBottomSheetBehaviour(bottomSheetBehaviorInput);
        });
        //endregion

        tvInputContainer.setOnClickListener(v -> {

            resetInput();
            selectedInputButton = Constants.BTN_TV;
            tvTitleInsertValue.setText(R.string.insert_tv_value);
            seekBarInputValue.setRange(100, 800, 1);
            etInputValue.setText(tvTvInputValue.getText());
            seekBarInputValue.setProgress(Float.parseFloat(tvTvInputValue.getText().toString()));
            setEnableTiAdjustView(false);
            toggleBottomSheetBehaviour(bottomSheetBehaviorInput);
        });


        //region value input update

        seekBarInputValue.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {

                if (selectedInputButton.equals(Constants.BTN_TI)) {
                    etInputValue.setText(String.format(Locale.ENGLISH, "%.1f", leftValue));
                } else {
                    etInputValue.setText(String.valueOf((int) leftValue));
                }
                if (selectedInputButton.equals(Constants.BTN_BPM)) {

                    Log.d(TAG, "onRangeChanged leftValue: " + leftValue);
                    float maxTi = (((60 / Float.parseFloat(etInputValue.getText().toString())) / 3) * 2);
                    seekBarTiInputValue.setRange(0.5f, maxTi);
                    seekBarTiInputValue.setProgress(maxTi / 2);
                    etTiInputValue.setText(String.format(Locale.ENGLISH, "%.1f", maxTi / 2));

                    Log.d(TAG, "onRangeChanged leftValue: " + leftValue);
                    Log.d(TAG, "onRangeChanged int leftValue: " + (int) leftValue);

                    float ieSum = 60 / Float.parseFloat(etInputValue.getText().toString());
                    float ti = Float.parseFloat(etTiInputValue.getText().toString());
                    float te = ieSum - ti;
                    tvIERatio.setText(calculateIERation(ti, te));


                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        seekBarTiInputValue.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {

                etTiInputValue.setText(String.format(Locale.ENGLISH, "%.1f", leftValue));

                float bpm = Float.parseFloat(etInputValue.getText().toString());
                float ieSum = 60 / bpm;
                float ti = Float.parseFloat(etTiInputValue.getText().toString());
                float te = ieSum - ti;
                tvIERatio.setText(calculateIERation(ti, te));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        btnUpdateValue.setOnClickListener(v -> {
            String inputValue = etInputValue.getText().toString();

            if (!inputValue.isEmpty()) {

                switch (selectedInputButton) {
                    case Constants.BTN_BPM:
                        if (Integer.parseInt(inputValue) < 10)
                            bpm = "0" + inputValue;
                        else
                            bpm = inputValue;
                        tvBpmInputValue.setText(bpm);
                        String tiValue = etTiInputValue.getText().toString();
                        tvTiInputValue.setText(tiValue);
                        ti = tiValue;
                        break;
                    case Constants.BTN_TI:
                        ti = String.format(Locale.ENGLISH, "%.1f", Float.parseFloat(inputValue));
                        tvTiInputValue.setText(inputValue);
                        break;
                    case Constants.BTN_PEEP:
                        if (Integer.parseInt(inputValue) < 10)
                            peep = "0" + inputValue;
                        else
                            peep = inputValue;
                        tvPeepInputValue.setText(peep);
                        break;
                    case Constants.BTN_PIP:
                        if (Integer.parseInt(inputValue) < 10)
                            pip = "0" + inputValue;
                        else
                            pip = inputValue;
                        tvPipInputValue.setText(pip);
                        break;
                    case Constants.BTN_TV:
                        tv = inputValue;
                        tvTvInputValue.setText(inputValue);
                        break;
                }
                if (isVentilatorOn()) {
                    writeToBtModule(combineInputData());
                }
                hideBottomSheet(bottomSheetBehaviorInput);
                shouldEnableStartSwitch();
            } else {
                Toast.makeText(this, R.string.toast_message_provide_valid_value, Toast.LENGTH_SHORT).show();
            }
        });

        //endregion

        //region alert input update
        seekBarAlertInputValue.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {

                etAlertInputValue.setText(String.valueOf((int) leftValue));
                String progress = etAlertInputValue.getText().toString();
                switch (selectedAlertInput) {
                    case Constants.ALERT_SELECTED_TV_HIGH:
                        tvTvAlertInputHigh.setText(progress);
                        break;
                    case Constants.ALERT_SELECTED_TV_LOW:
                        tvTvAlertInputLow.setText(progress);
                        break;
                    case Constants.ALERT_SELECTED_PIP_HIGH:
                        tvPipAlertInputHigh.setText(progress);
                        break;
                    case Constants.ALERT_SELECTED_PIP_LOW:
                        tvPipAlertInputLow.setText(progress);
                        break;
                    case Constants.ALERT_SELECTED_PEEP_HIGH:
                        tvPeepAlertInputHigh.setText(progress);
                        break;
                    case Constants.ALERT_SELECTED_PEEP_LOW:
                        tvPeepAlertInputLow.setText(progress);
                        break;
                }

            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });

        btAlertUpdate.setOnClickListener(v -> {

            String inputValue = etAlertInputValue.getText().toString();

            switch (selectedAlertInput) {
                case Constants.ALERT_SELECTED_TV_HIGH:
                    tvAlarmHigh = inputValue;
                    tvTvAlarmValueDisplayHigh.setText(tvAlarmHigh);
                    break;
                case Constants.ALERT_SELECTED_TV_LOW:
                    tvAlarmLow = inputValue;
                    tvTvAlarmValueDisplayLow.setText(tvAlarmLow);
                    break;
                case Constants.ALERT_SELECTED_PIP_HIGH:
                    pipAlarmHigh = inputValue;
                    tvPipAlarmValueDisplayHigh.setText(pipAlarmHigh);
                    break;
                case Constants.ALERT_SELECTED_PIP_LOW:
                    pipAlarmLow = inputValue;
                    tvPipAlarmValueDisplayLow.setText(pipAlarmLow);
                    break;
                case Constants.ALERT_SELECTED_PEEP_HIGH:
                    peepAlarmHigh = inputValue;
                    tvPeepAlarmValueDisplayHigh.setText(peepAlarmHigh);
                    break;
                case Constants.ALERT_SELECTED_PEEP_LOW:
                    peepAlarmLow = inputValue;
                    tvPeepAlarmValueDisplayLow.setText(peepAlarmLow);
                    break;
            }
            if (isVentilatorOn()) {
                writeToBtModule(combineInputData());
            }
            hideBottomSheet(bottomSheetBehaviorAlarm);
        });

        //endregion


        // region mode settings alarm control
        modeInputContainer.setOnClickListener(v -> {
            toggleBottomSheetBehaviour(bottomSheetBehaviorModes);
            if (activatedMode.equals(Constants.MODE_PRESSURE_SUPPORT_ACTIVATED)) {
                iconActivePressureMode();
            } else if (activatedMode.equals(Constants.MODE_PRVC_ACTIVATED)) {
                iconActivePrvcMode();
            }
        });

        settingsInputContainer.setOnClickListener(v -> {
            toggleBottomSheetBehaviour(bottomSheetBehaviorSettings);
        });

        alarmInputContainer.setOnClickListener(v -> {

            seekBarAlertInputValue.setEnabled(false);
            etAlertInputValue.setEnabled(false);
            toggleBottomSheetBehaviour(bottomSheetBehaviorAlarm);
        });

        //endregion

        //region mode selection
        pressureSupportMode.setOnClickListener(v -> {
            Toast.makeText(this, "Pressure support mode clicked", Toast.LENGTH_SHORT).show();
            clickedMode = Constants.MODE_PRESSURE_SUPPORT_MODE_CLICKED;

            customDialog = new CustomDialog(this);
            customDialog.createDialog("Activate Pressure Support Mode ", "Are you sure you want" +
                    " to activate pressure support mode ?", "No", "Yes");
            customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog) {

                    activatedMode = Constants.MODE_PRESSURE_SUPPORT_ACTIVATED;
                    Toast.makeText(GraphActivity.this, "Pressure Support Mode Activated", Toast.LENGTH_SHORT).show();
                    iconActivePressureMode();
                    tvInputContainer.setVisibility(View.GONE);
                    pipInputContainer.setVisibility(View.VISIBLE);
                    updateModeText("PSV");
                    changeMode("p");
                    if (isVentilatorOn()) {
                        writeToBtModule(combineInputData());
                    }
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            customDialog.showDialog();
        });
        prvcMode.setOnClickListener(v -> {
            Toast.makeText(this, "PRVC mode clicked", Toast.LENGTH_SHORT).show();
            clickedMode = Constants.MODE_PRVC_CLICKED;

            customDialog = new CustomDialog(this);
            customDialog.createDialog("Activate PRVC Mode ", "Are you sure you want" +
                    " to activate PRVC mode ?", "No", "Yes");
            customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog) {

                    activatedMode = Constants.MODE_PRVC_ACTIVATED;
                    Toast.makeText(GraphActivity.this, "PRVC Mode Activated", Toast.LENGTH_SHORT).show();
                    iconActivePrvcMode();
                    tvInputContainer.setVisibility(View.VISIBLE);
                    pipInputContainer.setVisibility(View.GONE);
                    updateModeText("PRVC");
                    changeMode("v");
                    if (isVentilatorOn()) {
                        writeToBtModule(combineInputData());
                    }
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            customDialog.showDialog();
        });
        cpapMode.setOnClickListener(v -> {
            Toast.makeText(this, "CPAP mode cliked", Toast.LENGTH_SHORT).show();
            clickedMode = Constants.MODE_CPAP_CLICKED;

            customDialog = new CustomDialog(this);
            customDialog.createDialog("Activate CPAP Mode ", "Are you sure you want" +
                    " to activate CPAP mode ?", "No", "Yes");
            customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog) {

                    activatedMode = Constants.MODE_CPAP_ACTIVATED;
                    Toast.makeText(GraphActivity.this, "CPAP Mode Activated", Toast.LENGTH_SHORT).show();
                    iconActiveCpapMode();
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            customDialog.showDialog();
        });
        simvMode.setOnClickListener(v -> {
            Toast.makeText(this, "SIMV mode cliked", Toast.LENGTH_SHORT).show();
            clickedMode = Constants.MODE_SIMV_CLICKED;

            customDialog = new CustomDialog(this);
            customDialog.createDialog("Activate SIMV Mode ", "Are you sure you want" +
                    " to activate SIMV mode ?", "No", "Yes");
            customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog) {

                    activatedMode = Constants.MODE_SIMV_ACTIVATED;
                    Toast.makeText(GraphActivity.this, "SIMV Mode Activated", Toast.LENGTH_SHORT).show();
                    iconActiveSimvMode();
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            customDialog.showDialog();
        });
        acMode.setOnClickListener(v -> {
            Toast.makeText(this, "A-C mode cliked", Toast.LENGTH_SHORT).show();
            clickedMode = Constants.MODE_AC_CLICKED;

            customDialog = new CustomDialog(this);
            customDialog.createDialog("Activate A-C Mode ", "Are you sure you want" +
                    " to activate A-C mode ?", "No", "Yes");
            customDialog.setOnButtonClicked(new CustomDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog) {
                    activatedMode = Constants.MODE_AC_ACTIVATED;
                    Toast.makeText(GraphActivity.this, "AC Mode Activated", Toast.LENGTH_SHORT).show();
                    iconActiveAcMode();
                    dialog.dismiss();
                }
                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            customDialog.showDialog();
        });
        //endregion

        //region alert input

        tvAlertInputHighContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(tvAlertInputHighContainer, tvAlertInputLowContainer, pipAlertInputHighContainer,
                    pipAlertInputLowContainer, peepAlertInputHighContainer, peepAlertInputLowContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_TV_HIGH;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvTvAlertInputHigh.getText().toString()));
            etAlertInputValue.setText(tvTvAlertInputHigh.getText());
            enableAlarmValueInputs();

        });

        tvAlertInputLowContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(tvAlertInputLowContainer, tvAlertInputHighContainer, pipAlertInputHighContainer,
                    pipAlertInputLowContainer, peepAlertInputHighContainer, peepAlertInputLowContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_TV_LOW;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvTvAlertInputLow.getText().toString()));
            etAlertInputValue.setText(tvTvAlertInputLow.getText());
            enableAlarmValueInputs();

        });

        pipAlertInputHighContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(pipAlertInputHighContainer, tvAlertInputHighContainer, tvAlertInputLowContainer,
                    pipAlertInputLowContainer, peepAlertInputHighContainer, peepAlertInputLowContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_PIP_HIGH;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvPipAlertInputHigh.getText().toString()));
            etAlertInputValue.setText(tvPipAlertInputHigh.getText());
            enableAlarmValueInputs();

        });

        pipAlertInputLowContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(pipAlertInputLowContainer, tvAlertInputHighContainer, tvAlertInputLowContainer,
                    pipAlertInputHighContainer, peepAlertInputHighContainer, peepAlertInputLowContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_PIP_LOW;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvPipAlertInputLow.getText().toString()));
            etAlertInputValue.setText(tvPipAlertInputLow.getText());
            enableAlarmValueInputs();

        });

        peepAlertInputHighContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(peepAlertInputHighContainer, tvAlertInputHighContainer, tvAlertInputLowContainer,
                    pipAlertInputHighContainer, pipAlertInputLowContainer, peepAlertInputLowContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_PEEP_HIGH;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvPeepAlertInputHigh.getText().toString()));
            etAlertInputValue.setText(tvPipAlertInputHigh.getText());
            enableAlarmValueInputs();

        });

        peepAlertInputLowContainer.setOnClickListener(v -> {
            changeSelectedAlertInputBackground(peepAlertInputLowContainer, tvAlertInputHighContainer, tvAlertInputLowContainer,
                    pipAlertInputHighContainer, pipAlertInputLowContainer, peepAlertInputHighContainer);
            selectedAlertInput = Constants.ALERT_SELECTED_PEEP_LOW;
            seekBarAlertInputValue.setRange(0, 1000);
            seekBarAlertInputValue.setProgress(Float.parseFloat(tvPeepAlertInputLow.getText().toString()));
            etAlertInputValue.setText(tvPeepAlertInputLow.getText());
            enableAlarmValueInputs();

        });


        //end region
        btSnoozeHighTvAlertDialog.setOnClickListener(v -> {
            isHighTvAlarmSnoozed = true;
            setHighAlarmSchedule(highAlarmDelayValue);
            highTvAlertDialog.setVisibility(View.GONE);
            switchSnoozeHighTvAlarm.setChecked(isHighTvAlarmSnoozed);
            if (soundPlayer != null && soundPlayer.isPlaying()) {
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }
        });
        btSnoozeLowTvAlertDialog.setOnClickListener(v -> {
            isLowTvAlarmSnoozed = true;
            setLowAlarmSchedule(lowAlarmDelayValue);
            lowTvAlertDialog.setVisibility(View.GONE);
            switchSnoozeLowTvAlarm.setChecked(isLowTvAlarmSnoozed);
            if (!isAnyAlarmDialogVisible()) {
                soundPlayer.pauseAlarm();
                setSnoozeAlarm(true);
            }
        });
        btSnoozeHighPeepAlertDialog.setOnClickListener(v -> {
            isHighPeepAlarmSnoozed = true;
            setHighAlarmSchedule(highAlarmDelayValue);
            highPeepAlertDialog.setVisibility(View.GONE);
            switchSnoozeHighPeepAlarm.setChecked(isHighPeepAlarmSnoozed);
            if (!isAnyAlarmDialogVisible()) {
                soundPlayer.pauseAlarm();
                setSnoozeAlarm(true);
            }
        });
        btSnoozeLowPeepAlertDialog.setOnClickListener(v -> {
            isLowPeepAlarmSnoozed = true;
            setLowAlarmSchedule(lowAlarmDelayValue);
            lowPeepAlertDialog.setVisibility(View.GONE);
            switchSnoozeLowPeepAlarm.setChecked(isLowPeepAlarmSnoozed);
            if (!isAnyAlarmDialogVisible()) {
                soundPlayer.pauseAlarm();
                setSnoozeAlarm(true);
            }
        });

        btSnoozeHighPiPAlertDialog.setOnClickListener(v -> {
            isHighPipAlarmSnoozed = true;
            setHighAlarmSchedule(highAlarmDelayValue);
            setLowAlarmSchedule(lowAlarmDelayValue);
            highPipAlertDialog.setVisibility(View.GONE);
            switchSnoozeHighPipAlarm.setChecked(isHighPipAlarmSnoozed);
            if (!isAnyAlarmDialogVisible()) {
                soundPlayer.pauseAlarm();
                setSnoozeAlarm(true);
            }
        });

        btSnoozeLowPiPAlertDialog.setOnClickListener(v -> {
            isLowPipAlarmSnoozed = true;
            lowPipAlertDialog.setVisibility(View.GONE);
            switchSnoozeLowPipAlarm.setChecked(isLowPipAlarmSnoozed);
            if (!isAnyAlarmDialogVisible()) {
                soundPlayer.pauseAlarm();
                setSnoozeAlarm(true);
            }
        });

        switchSnoozeHighTvAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighTvAlarmSnoozed = isChecked;
            if (isChecked) {
                highTvAlertDialog.setVisibility(View.GONE);
                switchSnoozeHighTvAlarm.setChecked(isHighTvAlarmSnoozed);
                if (soundPlayer != null && soundPlayer.isPlaying()) {
                    if (!isAnyAlarmDialogVisible()) {
                        soundPlayer.pauseAlarm();
                        setSnoozeAlarm(true);
                    }
                }
            }

        });
        switchSnoozeLowTvAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLowTvAlarmSnoozed = isChecked;
            if (isChecked) {
                lowTvAlertDialog.setVisibility(View.GONE);
                switchSnoozeLowTvAlarm.setChecked(isLowTvAlarmSnoozed);
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }
        });
        switchSnoozeHighPeepAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighPeepAlarmSnoozed = isChecked;
            if (isChecked) {
                highPeepAlertDialog.setVisibility(View.GONE);
                switchSnoozeHighPeepAlarm.setChecked(isHighPeepAlarmSnoozed);
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }
        });
        switchSnoozeLowPeepAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLowPeepAlarmSnoozed = isChecked;
            if (isChecked) {
                lowPeepAlertDialog.setVisibility(View.GONE);
                switchSnoozeLowPeepAlarm.setChecked(isLowPeepAlarmSnoozed);
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }
        });
        switchSnoozeHighPipAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isHighPipAlarmSnoozed = isChecked;
            if (isChecked) {
                highPipAlertDialog.setVisibility(View.GONE);
                switchSnoozeHighPipAlarm.setChecked(isHighPipAlarmSnoozed);
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }

        });
        switchSnoozeLowPipAlarm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isLowPipAlarmSnoozed = isChecked;
            if (isChecked) {
                lowPipAlertDialog.setVisibility(View.GONE);
                switchSnoozeLowPipAlarm.setChecked(isLowPipAlarmSnoozed);
                if (!isAnyAlarmDialogVisible()) {
                    soundPlayer.pauseAlarm();
                    setSnoozeAlarm(true);
                }
            }
        });

        btChangeLowAlarmDelay.setOnClickListener(v -> {

            PopupAlarmDialog lowAlarmDialog = new PopupAlarmDialog(this, lowAlarmDelayValue);
            lowAlarmDialog.setDialogTitle("Set low alarm dialog delay time (in minute)");
            lowAlarmDialog.showPopupAlarmAdjustDialog();
            lowAlarmDialog.setOnButtonClicked(new PopupAlarmDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog, int value) {
                    lowAlarmDelayValue = value;
                    tvLowAlarmDelayValue.setText(lowAlarmDelayValue + " min");
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        });

        btChangeHighAlarmDelay.setOnClickListener(v -> {

            PopupAlarmDialog highAlarmDialog = new PopupAlarmDialog(this, lowAlarmDelayValue);
            highAlarmDialog.setDialogTitle("Set high alarm dialog delay time (in minute)");
            highAlarmDialog.showPopupAlarmAdjustDialog();
            highAlarmDialog.setOnButtonClicked(new PopupAlarmDialog.OnButtonClicked() {
                @Override
                public void onPositiveButtonClicked(DialogInterface dialog, int value) {
                    highAlarmDelayValue = value;
                    tvLowAlarmDelayValue.setText(highAlarmDelayValue + " min");
                    dialog.dismiss();
                }

                @Override
                public void onNegativeButtonClicked(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        });

        // registering a broadcast listener for getting bluetooth changing update
        this.registerReceiver(connectionReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        this.registerReceiver(connectionReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

    }


    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        // stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        try {
            mmInputStream = btSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = mmInputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        mmInputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                                readBufferPosition = 0;
                                handler.post(() -> {
                                    Log.d(TAG, "bluetooth data " + data);
                                    if (data.length() > 50) {
                                        tvDebug.setText(data);
                                        if (isVentilatorOn()) {
                                            String[] splitData = data.split(",");
                                            if (splitData.length == 10) {
                                                Log.d(TAG, "beginListenForData: spleet data" + Arrays.toString(splitData));
                                                if (!TextUtils.isEmpty(splitData[2])) {
                                                    String inputData = splitData[2];
                                                    if (splitData[2] != null) {
                                                        fillInputData(inputData);
                                                    }
                                                }
                                                if (!TextUtils.isEmpty(splitData[6]) && Helper.isNumeric(splitData[6])) {
                                                    float flow = Float.parseFloat(splitData[6]);
                                                    helper.addEntry(flow, flowGraph, "Flow", "#6aa2ff");
                                                    MeasuredHelper.setValueIntoView(tvFlowValue, String.valueOf((int) flow));
                                                }
                                                if (!TextUtils.isEmpty(splitData[7]) && Helper.isNumeric(splitData[7])) {
                                                    float vt = Float.parseFloat(splitData[7]);

                                                    if (!TextUtils.isEmpty(splitData[0]) && !splitData[0].equals("@B")) {
                                                        helper.addEntry(vt, vtGraph, "Tidal Volume", "#6aff6a");
                                                    }

                                                    if (!TextUtils.isEmpty(splitData[0]) && splitData[0].equals("@B")) {
                                                        MeasuredHelper.setValueIntoView(tvVtValue, String.valueOf((int) vt));
                                                    }

                                                }
                                                //region tv alarm
                                                if (!TextUtils.isEmpty(splitData[3]) && splitData[3].equals("HV")) {

                                                    lowTvAlertDialog.setVisibility(View.GONE);
                                                    if (!isHighPeepAlarmSnoozed && shouldHighAlarmActivate) {
                                                        highTvAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }

                                                } else if (!TextUtils.isEmpty(splitData[3]) && splitData[3].equals("LV")) {

                                                    highTvAlertDialog.setVisibility(View.GONE);

                                                    if (!isLowTvAlarmSnoozed && shouldLowAlarmActivate) {
                                                        lowTvAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }
                                                }

                                                //region peep alarm
                                                if (!TextUtils.isEmpty(splitData[4]) && splitData[4].equals("HP")) {

                                                    lowPeepAlertDialog.setVisibility(View.GONE);
                                                    if (!isHighPeepAlarmSnoozed && shouldHighAlarmActivate) {
                                                        highPeepAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }


                                                } else if (!TextUtils.isEmpty(splitData[4]) && splitData[4].equals("LP")) {

                                                    highPeepAlertDialog.setVisibility(View.GONE);
                                                    if (!isLowPeepAlarmSnoozed && shouldLowAlarmActivate) {
                                                        lowPeepAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }
                                                }
                                                //region pip alarm
                                                if (!TextUtils.isEmpty(splitData[5]) && splitData[5].equals("HPP")) {

                                                    lowPipAlertDialog.setVisibility(View.GONE);
                                                    if (!isHighPipAlarmSnoozed && shouldHighAlarmActivate) {
                                                        highPipAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }

                                                } else if (!TextUtils.isEmpty(splitData[5]) && splitData[5].equals("LPP")) {

                                                    highPipAlertDialog.setVisibility(View.GONE);
                                                    if (!isLowPipAlarmSnoozed && shouldLowAlarmActivate) {
                                                        lowPipAlertDialog.setVisibility(View.VISIBLE);
                                                        if (!soundPlayer.isPlaying()) {
                                                            soundPlayer.playAlarm();

                                                        }
                                                    }
                                                }
                                                // else if ((!TextUtils.isEmpty(splitData[5]) && splitData[5].equals("NAA"))) {
                                                //     if (alarmController.isStarted()) {
                                                //         alarmController.pauseAlarm();
                                                //         alarmController.dismissDialog();
                                                //     }
                                                // }
                                                //endregion

                                                if (!TextUtils.isEmpty(splitData[8]) && Helper.isNumeric(splitData[8])) {
                                                    float pressure = Float.parseFloat(splitData[8]);
                                                    helper.addEntry(pressure, pressureGraph, "Pressure", "#FF7F21");
                                                    if (!TextUtils.isEmpty(splitData[0]) && splitData[0].equals("@B")) {
                                                        updatePeakPressure(pressure);
                                                    }
                                                    if (!TextUtils.isEmpty(splitData[0]) && splitData[0].equals("@L")) {
                                                        // Toast.makeText(this, data, Toast.LENGTH_LONG).show();
                                                        tvPeepValue.setText(String.valueOf(pressure));
                                                    }
                                                    Helper.setPressureData(pressure, bcPressure, splitData[0]);
                                                }
                                                // if (!TextUtils.isEmpty(splitData[7]) && Helper.isNumeric(splitData[7])) {
                                                //     float pressure = Float.parseFloat(splitData[8]);
                                                //     helper.addEntry(pressure, pressureGraph, "Pressure", "#FF7F21");
                                                // }


                                                tvBpmValue.setText(bpm);
                                                // // tvPipValue.setText("4.57");
                                                // // tvOxygenValue.setText("42.0");
                                                // MeasuredHelper.setValueIntoView(tvPressureValue, String.valueOf(pressure));
                                            }
                                        }


                                    }
                                    // feedMultiple(Float.parseFloat(data));
                                });
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException ex) {

                    Log.d(TAG, "beginListenForData worker thread exception: " + ex.getMessage());
                    stopWorker = true;
                }
            }
        });
        workerThread.start();
    }

    private void toggleBottomSheetBehaviour(BottomSheetBehavior bottomSheetBehavior) {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void prepareGraph(LineChart graph) {
        graph.getAxisLeft().setTextColor(Color.WHITE);
        graph.getXAxis().setTextColor(Color.WHITE);
        graph.getLegend().setTextColor(Color.WHITE);
        graph.getDescription().setTextColor(Color.WHITE);
        graph.getDescription().setEnabled(false);
        graph.getAxisLeft().setDrawGridLines(false);        // hide background grid
        graph.getXAxis().setDrawGridLines(false);           // hide background grid
        graph.getAxisRight().setDrawGridLines(false);       // hide background grid
        // graph.getAxisLeft().setDrawLabels(false);
        graph.getAxisRight().setDrawLabels(false);
        graph.getXAxis().setDrawLabels(false);
        // graph.setMaxVisibleValueCount(500);
        graph.setTouchEnabled(false);
        graph.setPinchZoom(false);
        // graph.getAxisLeft().setGranularity(20);
        // graph.getAxisLeft().setGranularityEnabled(true);
        graph.setScaleYEnabled(true);
        graph.setScaleY(1f);
        graph.setScaleX(1f);


    }

    private void writeToBtModule(String value) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(value.getBytes());
                Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetInput() {
        etInputValue.setText("");
    }

    private void hideBottomSheet(BottomSheetBehavior bottomSheetBehavior) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void updatePeakPressure(float currentPressure) {
        if (currentPressure > peakPressure) {
            peakPressure = currentPressure;
            tvPeakPressureValue.setText(String.valueOf(peakPressure));
        }

    }

    private void fillInputData(String inputData) {

        if (inputData.length() == 27) {
            mode = inputData.substring(0, 1);
            bpm = inputData.substring(1, 3);
            tv = inputData.substring(3, 6);
            ti = inputData.substring(6, 9);
            peep = inputData.substring(9, 11);
            pip = inputData.substring(11, 13);
            tvAlarmHigh = inputData.substring(13, 16);
            tvAlarmLow = inputData.substring(16, 19);
            pipAlarmHigh = inputData.substring(19, 21);
            pipAlarmLow = inputData.substring(21, 23);
            peepAlarmHigh = inputData.substring(23, 25);
            peepAlarmLow = inputData.substring(25, 27);
        } else {
            // Toast.makeText(this, "Malform input frame", Toast.LENGTH_SHORT).show();
        }
    }

    private String combineInputData() {
        String inputFrame =
                mode + bpm + tv + ti + peep + pip + tvAlarmHigh + tvAlarmLow + pipAlarmHigh + pipAlarmLow + peepAlarmHigh
                        + peepAlarmLow;
        Log.d(TAG, "combineInputData: " + inputFrame);
        Toast.makeText(this, inputFrame, Toast.LENGTH_LONG).show();
        return inputFrame;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        workerThread.interrupt();
    }

    void iconActivePressureMode() {
        ivPressureSupportIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_activated));
        ivPrvcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivCpapModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivSimvModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivAcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
    }

    void iconActivePrvcMode() {
        ivPrvcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_activated));
        ivPressureSupportIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivCpapModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivSimvModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivAcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
    }

    void iconActiveCpapMode() {
        ivCpapModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_activated));
        ivPrvcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivPressureSupportIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivSimvModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivAcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
    }

    void iconActiveSimvMode() {
        ivSimvModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_activated));
        ivCpapModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivPrvcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivPressureSupportIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivAcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
    }

    void iconActiveAcMode() {
        ivAcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_activated));
        ivCpapModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivPrvcModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivPressureSupportIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
        ivSimvModeIcon.setColorFilter(ContextCompat.getColor(this, R.color.color_mode_deactivated));
    }


    // broadcast listener
    private final BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                ivConnectionStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.red_circle_background));
                tvConnectionText.setText(R.string.disconnected);
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                ivConnectionStatus.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.green_circle_background));
            }
        }
    };


    private void changeSelectedAlertInputBackground(View selectedView, View... unSelectedViews) {
        selectedView.setBackground(ContextCompat.getDrawable(selectedView.getContext(), R.drawable.alert_input_selected_color));

        for (View unSelectedView : unSelectedViews) {
            unSelectedView.setBackground(ContextCompat.getDrawable(selectedView.getContext(),
                    R.drawable.alert_input_default_color));
        }
    }

    private void displayAlarmValue() {

    }

    private void setSeekBarMinMaxStep(float min, float max, float step) {
        seekMin = min;
        seekMax = max;
        seekStep = step;
    }

    private void setInitialInputValueInViews() {

        tvBpmInputValue.setText(bpm);
        tvTiInputValue.setText(ti);
        tvPeepInputValue.setText(peep);
        tvPipInputValue.setText(pip);
        tvTvInputValue.setText(tv);

        tvTvAlarmValueDisplayHigh.setText(tvAlarmHigh);
        tvTvAlarmValueDisplayLow.setText(tvAlarmLow);
        tvPipAlarmValueDisplayHigh.setText(pipAlarmHigh);
        tvPipAlarmValueDisplayLow.setText(pipAlarmLow);
        tvPeepAlarmValueDisplayHigh.setText(peepAlarmHigh);
        tvPeepAlarmValueDisplayLow.setText(peepAlarmLow);

        tvTvAlertInputHigh.setText(tvAlarmHigh);
        tvTvAlertInputLow.setText(tvAlarmLow);
        tvPipAlertInputHigh.setText(pipAlarmHigh);
        tvPipAlertInputLow.setText(pipAlarmLow);
        tvPeepAlertInputHigh.setText(peepAlarmHigh);
        tvPeepAlertInputLow.setText(peepAlarmLow);

        tvLowAlarmDelayValue.setText(lowAlarmDelayValue + " min");
        tvHighAlarmDelayValue.setText(highAlarmDelayValue + " min");

        iconActivePressureMode();
    }

    private void updateModeText(String text) {
        tvCurrentModeText.setText(text);
    }

    private void changeMode(String modeValue) {
        mode = modeValue;
    }

    private boolean shouldEnableStartSwitch() {

        if (activatedMode.equals(Constants.MODE_PRESSURE_SUPPORT_ACTIVATED)) {
            if (
                    Helper.isNumeric(tvBpmInputValue.getText().toString()) &&
                            Helper.isNumeric(tvTiInputValue.getText().toString()) &&
                            Helper.isNumeric(tvPeepInputValue.getText().toString()) &&
                            Helper.isNumeric(tvPipInputValue.getText().toString())
            ) {
                switchOnOff.setEnabled(true);
                return true;
            } else {

                switchOnOff.setEnabled(false);
                Toast.makeText(this, "Please provide all the input fields for Pressure Support mode", Toast.LENGTH_LONG).show();
                return false;
            }
        } else if (activatedMode.equals(Constants.MODE_PRVC_ACTIVATED)) {
            if (
                    Helper.isNumeric(tvBpmInputValue.getText().toString()) &&
                            Helper.isNumeric(tvTiInputValue.getText().toString()) &&
                            Helper.isNumeric(tvPeepInputValue.getText().toString()) &&
                            Helper.isNumeric(tvTvInputValue.getText().toString())
            ) {
                switchOnOff.setEnabled(true);
                return true;
            } else {
                switchOnOff.setEnabled(false);
                Toast.makeText(this, "Please provide all the input fields for PRVC mode", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectionReceiver);
    }

    private boolean isVentilatorOn() {
        return isVentilatorOn;
    }

    float gcd(float p, float q) {
        if (q == 0) return p;
        else return gcd(q, p % q);
    }

    String ratio(float a, float b) {
        final float gcd = gcd(a, b);

        float aRatio = (int) (a / gcd);
        float bRatio = (int) (b / gcd);
        float divisor = Math.min(aRatio, bRatio);

        float aFinalRatio = aRatio / divisor;
        float bFinalRatio = bRatio / divisor;

        String aRatioString;
        if (aFinalRatio % 1 == 0)
            aRatioString = String.format(Locale.ENGLISH, "%.0f", aFinalRatio);
        else aRatioString = String.format(Locale.ENGLISH, "%.1f", aFinalRatio);
        String bRationString;
        if (bFinalRatio % 1 == 0)
            bRationString = String.format(Locale.ENGLISH, "%.0f", bFinalRatio);
        else bRationString = String.format(Locale.ENGLISH, "%.1f", bFinalRatio);

        return aRatioString + " : " + bRationString;
    }

    String calculateIERation(float num1, float num2) {
        float num1OneDecimal = Float.parseFloat(String.format(Locale.ENGLISH, "%.1f", num1)) * 10;
        float num2OneDecimal = Float.parseFloat(String.format(Locale.ENGLISH, "%.1f", num2)) * 10;
        return ratio(num1OneDecimal, num2OneDecimal);
    }

    void setEnableTiAdjustView(boolean enabled) {
        if (enabled) {
            ieRatioContainer.setVisibility(View.VISIBLE);
            autoTiContainer.setVisibility(View.VISIBLE);
        } else {
            ieRatioContainer.setVisibility(View.GONE);
            autoTiContainer.setVisibility(View.GONE);
        }
    }

    void setSnoozeAlarm(boolean setEnabled) {
        this.isAlarmSnoozed = setEnabled;
    }

    boolean isAnyAlarmDialogVisible() {
        return highTvAlertDialog.getVisibility() == View.VISIBLE
                || lowTvAlertDialog.getVisibility() == View.VISIBLE
                || highPeepAlertDialog.getVisibility() == View.VISIBLE
                || lowPeepAlertDialog.getVisibility() == View.VISIBLE
                || highPipAlertDialog.getVisibility() == View.VISIBLE
                || lowPipAlertDialog.getVisibility() == View.VISIBLE;
    }

    void enableAlarmValueInputs() {
        seekBarAlertInputValue.setEnabled(true);
        etAlertInputValue.setEnabled(true);
    }

    void setLowAlarmSchedule(int minute) {
        Handler lowAlarmHandler = new Handler();
        lowAlarmHandler.removeCallbacksAndMessages(null);
        lowAlarmHandler.postDelayed(() -> {
            shouldLowAlarmActivate = true;
            switchSnoozeLowTvAlarm.setChecked(false);
            switchSnoozeLowPipAlarm.setChecked(false);
            switchSnoozeLowPeepAlarm.setChecked(false);
        }, minute * 60 * 1000);

    }

    void setHighAlarmSchedule(int minute) {
        Handler highAlarmHandler = new Handler();
        highAlarmHandler.removeCallbacksAndMessages(null);
        highAlarmHandler.postDelayed(() -> {
            shouldHighAlarmActivate = true;
            switchSnoozeHighTvAlarm.setChecked(false);
            switchSnoozeHighPipAlarm.setChecked(false);
            switchSnoozeHighPeepAlarm.setChecked(false);
        }, minute * 60 * 1000);

    }

}