package com.bricechou.bluetoothpbapclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ClientSocketActivity extends Activity {
    private static final String TAG = ClientSocketActivity.class.getSimpleName();
    private static final int REQUEST_DISCOVERY = 0x1;
    private static final int BIT_MASK = 0x000000ff;
    private Handler _handler = new Handler();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.activity_client_socket);
        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }
        Intent intent = new Intent(this, DiscoveryActivity.class);
        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
        startActivityForResult(intent, REQUEST_DISCOVERY);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_DISCOVERY) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        new Thread() {
            public void run() {
                connect(device);
            }
        }.start();
    }

    protected void connect(BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            // socket = device.createRfcommSocketToServiceRecord(BluetoothProtocols.OBEX_OBJECT_PUSH_PROTOCOL_UUID);
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("0000112f-0000-1000-8000-00805f9b34fb"));
            // socket = device.createRfcommSocketToServiceRecord(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
            socket.connect();
            OutputStream os = socket.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write((byte) 0x80);// get(也可以为03)
            baos.write((byte) 0x00);// 整个数据包的长度 高字节
            baos.write((byte) 0x1A);
            baos.write((byte) 0x10);
            baos.write((byte) 0x00);
            baos.write((byte) 0x40);
            baos.write((byte) 0x06);
            baos.write((byte) 0x46);
            baos.write((byte) 0x00);
            baos.write((byte) 0x13);
            // Target ID :796135f0-f0c5-11d8-0966-0800200c9a66
            baos.write((byte) 0x79);
            baos.write((byte) 0x61);
            baos.write((byte) 0x35);
            baos.write((byte) 0xf0);
            baos.write((byte) 0xf0);
            baos.write((byte) 0xc5);
            baos.write((byte) 0x11);
            baos.write((byte) 0xd8);
            baos.write((byte) 0x09);
            baos.write((byte) 0x66);
            baos.write((byte) 0x08);
            baos.write((byte) 0x00);
            baos.write((byte) 0x20);
            baos.write((byte) 0x0c);
            baos.write((byte) 0x9a);
            baos.write((byte) 0x66);

           /* baos.write((byte) 0xc3);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0xf4);
            baos.write((byte) 0x83);*/
            /*
            //00 00 00 40 13 4E
            baos.write((byte) 0xCB);// connect id
            baos.write((byte) 0x01);//name
            byte[] nameBytes = new byte[]{
                    0x00, 0x74, 0x00, 0x65, 0x00, 0x6c, 0x00, 0x65, 0x00, 0x63, 0x00, 0x6f, 0x00, 0x6d, 0x00,
                    0x2f, 0x00, 0x70, 0x00, 0x62, 0x00, 0x2e, 0x00, 0x76, 0x00, 0x63, 0x00, 0x66
            };
            short nameLength = (short) (nameBytes.length + 2 + 3);
            byte[] nameLengthBytes = ConvertByteUtils.shortToBytes(nameLength);
            baos.write(nameLengthBytes);//name 长度2字节
            baos.write(nameBytes);//name的长度 高字节
            baos.write((byte) 0x00);//name结束
            baos.write((byte) 0x00);//name结束

            baos.write((byte) 0x42);//type
            byte[] typeBytes = "x-bt/phonebook".getBytes("UTF-8");
            short typeLength = (short) (typeBytes.length + 1 + 3);
            byte[] typeLengthBytes = ConvertByteUtils.shortToBytes(typeLength);
            baos.write(typeLengthBytes);//type长度2字节
            baos.write(typeBytes);//type name
            baos.write((byte) 0x00);//type name 结束

            baos.write((byte) 0x4c);//app params
            baos.write((byte) 0x00);//app params 的长度 高字节
            baos.write((byte) 0x14);//app params 的长度 低字节
            baos.write((byte) 0x06);//此处为pbap自定义,表示vcardfilter
            baos.write((byte) 0x08);// 8位
            baos.write(new byte[8]);// 64位掩码.需要的话请看spec.全为0,则返回所有的
            baos.write((byte) 0x07);//  vcard 版本
            baos.write((byte) 0x01);//  长度
            baos.write((byte) 0x01);//  01= 3.1  00 = 2.0
            baos.write((byte) 0x04);//maxlistcount 取多少个
            baos.write((byte) 0x02);// 长度
            baos.write((byte) 0xff);// ffff表示取所有的.
            baos.write((byte) 0xff);// ffff表示取所有的.
            */
            byte[] phoneBookDownloadReq = baos.toByteArray();
            Log.i(TAG, "Send datas = " + ConvertByteUtils.bytesToHexString(phoneBookDownloadReq));
            os.write(phoneBookDownloadReq);
            os.flush();

            InputStream is = socket.getInputStream();
            ByteArrayOutputStream baosR = new ByteArrayOutputStream();
            int ch = -1;
            byte[] buffer = new byte[2048];
            while ((ch = is.read(buffer)) != -1) {
                baosR.write(buffer, 0, ch);
                byte[] resp = baosR.toByteArray();
                Log.i(TAG, "Get datas = " + ConvertByteUtils.bytesToHexString(resp));
            }

        } catch (IOException e) {
            Log.e(TAG, "", e);
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }
}