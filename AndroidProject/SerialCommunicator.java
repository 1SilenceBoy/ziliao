package com.example.serialport;

import android.app.Application;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import cn.wch.uartlib.WCHUARTManager;
import cn.wch.uartlib.callback.IUsbStateChange;
import cn.wch.uartlib.exception.ChipException;
import cn.wch.uartlib.exception.NoPermissionException;
import cn.wch.uartlib.exception.UartLibException;

//这个只需要new出来，然后确定获取到权限后，调用click函数，传入x,y坐标就行。目前只提供了click函数，其余函数均不应该被调用
public class SerialCommunicator {
    private WCHUARTManager serialManager = null;
    private Application application = null;
    private UsbDevice serialDevice = null;
    private TextView testLogView = null;
    private Exception exception = null;
    private volatile boolean hasPermission = false;
    private final int SLIDE = 1;
    private final int CLICK = 2;
    private final int vendorId = 0x1A86;
    private final int productId = 0x7523;

    //参数2，就是用来反馈一些信息的，比如获取权限失败，就会把这些信息，设置到这个view上面
    public SerialCommunicator(Application app, TextView view) {
        application = app;
        testLogView = view;
        monitorUSBState();
    }

    public void click( int x, int y) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte[] data = new byte[5];
        data[0] = CLICK;

        bb.asIntBuffer().put(x);

        data[1] = (byte)(x&0xff);
        data[2] = (byte)(x>>8&0xff);

        bb.clear();
        bb.asIntBuffer().put(y);

        data[3] =  (byte)(x&0xff);
        data[4] =(byte)(x>>8&0xff);

        write(data);
    }

    public boolean isMobileSerialSupported() {
        boolean support = application.getPackageManager().hasSystemFeature(
                "android.hardware.usb.host");

        return support;
    }

    public void serialDeviceDetachCallBack(UsbDevice device) {
        if (device.getVendorId() != vendorId || device.getProductId() != productId) {
            return;
        }

        if (serialDevice != null && serialManager.isConnected(serialDevice)) {
            serialManager.disconnect(serialDevice);
        }


        hasPermission = false;

        serialDevice = null;
    }

    public void serialDeviceAttachCallBack(UsbDevice device) {
        if (device.getVendorId() != vendorId) {
            testLogView.setText("usb device is not valid");
            return;
        }

//        if (serialManager.isConnected(device)) {
//            hasPermission = true;
//            return;
//        }

        serialDevice = device;

        requestPermission(serialDevice);
    }

    private void monitorUSBState() {
        serialManager = WCHUARTManager.getInstance();
        if (serialManager == null) {
            testLogView.setText("init error");
            return;
        } else {
            testLogView.setText("init start");
        }

        serialManager.init(application);

        WCHUARTManager.getInstance().setUsbStateListener(new IUsbStateChange() {
            @Override
            public void usbDeviceDetach(UsbDevice device) {
                serialDeviceDetachCallBack(device);
                testLogView.setText("device detach");
            }

            @Override
            public void usbDeviceAttach(UsbDevice device) {
                serialDeviceAttachCallBack(device);
            }

            @Override
            public void usbDevicePermission(UsbDevice device, boolean result) {
                if (device.getVendorId() == vendorId) {
                    hasPermission = result;
                    testLogView.setText("has permission");
                    try {
                        serialManager.openDevice(serialDevice);
                        serialManager.setSerialParameter(serialDevice, 0, 115200, 8, 1, 0, false);
                    } catch (Exception e) {
                        testLogView.setText(e.getMessage());
                    }
                }
            }
        });
    }


    private void requestPermission(UsbDevice serialDevice) {
        try {
            serialManager.requestPermission(application, serialDevice);
            testLogView.setText("start request permission");
        } catch (Exception e) {
            testLogView.setText(e.getMessage());
        }
    }

    private void write(byte[] data) {
        int ret = 0;
        try {
            ret = serialManager.writeData(serialDevice, 0, data, data.length, 0xffff);
        } catch (Exception e) {
            this.exception = e;
        }
    }

    //要先获取是否拥有权限操作操作串口设备。不过我一般是先通过窗口获取到权限之后，才发送点击命令，所以一般我不调用这个函数
    public boolean isHasPermission(){
        return hasPermission;
    }

}
