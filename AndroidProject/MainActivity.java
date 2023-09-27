package com.example.serialport;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import cn.wch.uartlib.WCHUARTManager;
import cn.wch.uartlib.exception.ChipException;
import cn.wch.uartlib.exception.NoPermissionException;
import cn.wch.uartlib.exception.UartLibException;

public class MainActivity extends AppCompatActivity {
    TextView lblTitle = null;
    SerialCommunicator serialCommunicator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView x = findViewById(R.id.t1);
//这个类的用法，看类的解释
        serialCommunicator = new SerialCommunicator(this.getApplication(), x);

        Button btn1 = findViewById(R.id.btn);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    serialCommunicator.click(300, 300);
                } catch (Exception e) {
                    x.setText(e.toString());
                }
            }
        });
    }


}