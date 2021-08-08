package com.crux.ventapp;

import java.util.List;


import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


class DeviceListAdapter extends BaseAdapter{

    //declaring necessary objects
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;
    private OnConnectButtonClickListener cnctBtnListener;

    // constructor
    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public void setCnctBtnListener (OnConnectButtonClickListener listener){
        cnctBtnListener =listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView			=  mInflater.inflate(R.layout.list_item_device, null);

            holder 				= new ViewHolder();

            holder.deviceName		=  (TextView) convertView.findViewById(R.id.device_name);
            holder.deviceAddress 	=  (TextView) convertView.findViewById(R.id.device_address);
            holder.pairBtn		    =  (Button) convertView.findViewById(R.id.btn_pair);
            holder.connectBtn       =  (Button) convertView.findViewById(R.id.connectBtn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device	= mData.get(position);

        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");

        // adding some more intent event to the broadcast receiver
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        //holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds((device.getBondState() == BluetoothDevice.BOND_BONDED) ?
        if((device.getBondState() == BluetoothDevice.BOND_BONDED)){

            Drawable img = convertView.getContext().getResources().getDrawable(R.drawable.bt_pair);
            holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
        }
        else{
            Drawable img = convertView.getContext().getResources().getDrawable(R.drawable.bt_unpair);
            holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
        }
        holder.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPairButtonClick(position);
                }
            }
        });

        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cnctBtnListener!=null){
                    cnctBtnListener.onConnectedButtonClick(position,v);

                }

            }
        });



        return convertView;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        Button pairBtn;
        Button connectBtn;
    }

    public interface OnPairButtonClickListener {
        void onPairButtonClick(int position);
    }

    public  interface OnConnectButtonClickListener{
        void onConnectedButtonClick(int position,View view);
    }
    // Create a BroadcastReceiver for ACTION_FOUND.
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Toast.makeText(context, "RECEIVER CALLED!!", Toast.LENGTH_LONG).show();


            if(intent.getAction().equals(
                    "android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED")){

                // code for Bluetooth connect

                Toast.makeText(context, "CONNECTED!!", Toast.LENGTH_LONG).show();
            }

            if(intent.getAction().equals(
                    "android.bluetooth.device.action.ACL_DISCONNECTED")){

                //code for Bluetooth disconnect;

            }
        }
    };
}