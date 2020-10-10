package com.crux.ventapp.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.crux.ventapp.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder> {

    private static final String TAG = "DeviceListAdapter";
    private List<BluetoothDevice> devices;
    private OnPairButtonClickListener onPairButtonClickListener;
    private OnConnectButtonClickListener onConnectButtonClickListener;

    public DeviceListAdapter(List<BluetoothDevice> devices){

        Log.d(TAG, "DeviceListAdapter: called");
        this.devices = devices;
    }


    @NonNull
    @Override
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View deviceListView = inflater.inflate(R.layout.list_item_device, parent, false);
        return new DeviceListViewHolder(deviceListView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListViewHolder holder, int position) {

        BluetoothDevice device = devices.get(holder.getAdapterPosition());

        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");

        // adding some more intent event to the broadcast receiver
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        //holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds((device.getBondState() == BluetoothDevice.BOND_BONDED) ?
        if((device.getBondState() == BluetoothDevice.BOND_BONDED)){

            Drawable img = holder.pairBtn.getContext().getResources().getDrawable(R.drawable.bt_pair);
            holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
        }
        else{
            Drawable img = holder.pairBtn.getContext().getResources().getDrawable(R.drawable.bt_unpair);
            holder.pairBtn.setCompoundDrawablesWithIntrinsicBounds(img,null,null,null);
        }
        holder.pairBtn.setOnClickListener(v -> {
            if (onPairButtonClickListener != null) {
                onPairButtonClickListener.onPairButtonClick(holder.getAdapterPosition());
            }
        });

        holder.connectBtn.setOnClickListener(v -> {
            if(onConnectButtonClickListener!=null){
                onConnectButtonClickListener.onConnectedButtonClick(holder.getAdapterPosition(),v);

            }

        });

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: "+devices.size());
        return devices.size();
    }

    public interface OnPairButtonClickListener {
        void onPairButtonClick(int position);
    }

    public interface OnConnectButtonClickListener {
        void onConnectedButtonClick(int position, View view);
    }

    public void setOnConnectButtonClickListener(OnConnectButtonClickListener onConnectButtonClickListener) {
        this.onConnectButtonClickListener = onConnectButtonClickListener;
    }

    public void setOnPairButtonClickListener(OnPairButtonClickListener onPairButtonClickListener) {
        this.onPairButtonClickListener = onPairButtonClickListener;
    }

    static class DeviceListViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        TextView deviceAddress;
        AppCompatButton pairBtn;
        AppCompatButton connectBtn;

        public DeviceListViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
            pairBtn = itemView.findViewById(R.id.btn_pair);
            connectBtn = itemView.findViewById(R.id.connectBtn);
        }


    }
}




