package com.nexfi.yuanpeigen.nexfi_android_ble.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.MainActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;

/**
 * Created by Mark on 2016/5/6.
 */
public class FragmentDialogConnected extends DialogFragment {

    private Button btn_continue;
    private AlertDialog alertDialog;
    Node node = MainActivity.getNode();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_connected_timeout, null);
        btn_continue = (Button) view.findViewById(R.id.btn_continue);
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                node.start();
                alertDialog.dismiss();
            }
        });
        builder.setView(view);
        alertDialog = builder.show();
        return alertDialog;
    }
}
