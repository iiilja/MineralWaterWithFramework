package ee.promobox.promoboxandroid.widgets;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import ee.promobox.promoboxandroid.R;
import ee.promobox.promoboxandroid.WifiActivity;
import ee.promobox.promoboxandroid.util.InternetConnectionUtil.WifiData;

/**
 * Created by ilja on 17.02.2015.
 */
public class WifiPasswordDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

    private static final int[] levels = {
            R.string.signal_strength_low,
            R.string.signal_strength_weak,
            R.string.signal_strength_medium,
            R.string.signal_strength_normal,
            R.string.signal_strength_strong };

    private EditText passwordEditText;
    private WifiData wifiData;
    private Button connect;
    private int listViewElementPosition;


    public static WifiPasswordDialog newInstance(WifiData wifiData, int listViewElementPosition) {
        WifiPasswordDialog frag = new WifiPasswordDialog();
        Bundle args = new Bundle();
        args.putSerializable("wifiData", wifiData);
        args.putInt("listViewElementPosition", listViewElementPosition);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        wifiData = (WifiData) getArguments().getSerializable("wifiData");
        listViewElementPosition = getArguments().getInt("listViewElementPosition");

        View v = inflater.inflate(R.layout.wifi_password_dialog_layout, container, false);
        TextView wifiNameTextView = (TextView) v.findViewById(R.id.network_wifi_dialog_wifi_name);
        TextView signalStrengthTextView = (TextView) v.findViewById(R.id.network_wifi_dialog_signal_strength);
        TextView signalSecurityTextView = (TextView) v.findViewById(R.id.network_wifi_dialog_security);

        passwordEditText = (EditText) v.findViewById(R.id.network_wifi_dialog_password);
        passwordEditText.addTextChangedListener(this);
        CheckBox showPasswordCheckBox = (CheckBox) v.findViewById(R.id.network_wifi_dialog_show_password);

        Button cancel = (Button) v.findViewById(R.id.network_wifi_dialog_cancel);
        connect = (Button) v.findViewById(R.id.network_wifi_dialog_connect);
        connect.setEnabled(false);
        cancel.setOnClickListener(this);
        connect.setOnClickListener(this);



        wifiNameTextView.setText(wifiData.getName());
        signalStrengthTextView.setText(levels[wifiData.getSignalStrength()]);
        signalSecurityTextView.setText(wifiData.getSecurityMode());
        showPasswordCheckBox.setOnCheckedChangeListener(this);


        return v;
    }


    @Override
    public void onClick(View v) {
        WifiActivity wifiActivity = (WifiActivity) getActivity();
        switch (v.getId()) {
            case R.id.network_wifi_dialog_connect:
                String password = passwordEditText.getText().toString();
                wifiActivity.connectToWifi(wifiData, password, listViewElementPosition);
                break;
            case R.id.network_wifi_dialog_cancel:
                break;
            default:
                break;
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() >= 8) {
            connect.setEnabled(true);
        } else {
            connect.setEnabled(false);
        }
    }
}
