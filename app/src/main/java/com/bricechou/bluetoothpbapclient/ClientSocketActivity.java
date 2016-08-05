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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ClientSocketActivity extends Activity {
    private static final String TAG = ClientSocketActivity.class.getSimpleName();
    private static final int REQUEST_DISCOVERY = 0x1;
    private static final int OBEX_RESPONSE_OK = 0xa0;
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
        Toast.makeText(this, "Select device to connect ... ...", Toast.LENGTH_SHORT).show();
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
            @Override
            public void run() {
                connect(device);
            }
        }.start();
    }

    protected ByteArrayOutputStream getVcard() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            /**
             * Field Opcode GET (0x03 or 0x83) M
             Field Packet Length Varies M
             Header Connection ID Varies M
             Header Single Response Mode 0x01 C1
             Header   0x01 C2
             Header Name Object name (*.vcf) or M
             X-BT-UID (X-BT-UID:*)
             Header Type “x-bt/vcard” M
             Header Application Parameters Varies O
             - PropertySelector Varies O
             - Format
             C1: The Single Response Mode header is mandatory in the first packet if GOEP2.0 or later is used else excluded
             (X).
             C2: The   header is optional if Single Response Mode is used else excluded (X).
             */
            baos.write((byte) 0x83); //Get Header 0x83

            baos.write((byte) 0x00); //Packet Length
            baos.write((byte) 0x4F);

            baos.write((byte) 0xCB);//Connection ID   0xCB
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x01);

            baos.write((byte) 0x01);//Name
            baos.write((byte) 0x00);
            baos.write((byte) 0x21);

//            char[] cs = new char[]{'t', 'e', 'l', 'e', 'c', 'o', 'm', '/', 'p', 'b', '.', 'v', 'c', 'f'};//要转换的char数组
            char[] cs = new char[]{'t', 'e', 'l', 'e', 'c', 'o', 'm', '/','p','b','/'};
            String str = new String(cs);
            // Only use Unicode encode
            byte[] bs = str.getBytes("Unicode");
            baos.write(bs);

            baos.write((byte) 0x42);//Type
            baos.write((byte) 0x00);
            baos.write((byte) 0x12);

//            char[] cs2 = new char[]{'x', '-', 'b', 't', '/', 'p', 'h', 'o', 'n', 'e', 'b', 'o', 'o', 'k'};//要转换的char数组
//            char[] cs2 = new char[]{'x', '-', 'b', 't', '/', 'v', 'c', 'a', 'r', 'd'};
//            char[] cs2 = new char[]{'t', 'e', 'x', 't', '/', 'x','-','v', 'C', 'a', 'r', 'd'};
            char[] cs2 = new char[]{'x', '-', 'b', 't', '/', 'v', 'c', 'a', 'r', 'd','-', 'l','i', 's','t','i','n','g'};
            String str2 = new String(cs2);
            // Do not to change the encode.
            byte[] bs2 = str2.getBytes(); // Do not add UTF-8 or Unicode
            baos.write(bs2);
            baos.write((byte) 0x00); //type name 结束

            /*baos.write((byte) 0x4C); //app params
            baos.write((byte) 0x00);//app params 的长度 高字节
            baos.write((byte) 0x14);//app params 的长度 低字节

            baos.write((byte) 0x06);// 此处为pbap自定义,表示vcardfilter
            baos.write((byte) 0x08);// 8位

            baos.write((byte) 0x00);// 64位掩码.需要的话请看spec.全为0,则返回所有的,不对vcard做任何过滤
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);

            baos.write((byte) 0x07); //  vcard 版本
            baos.write((byte) 0x01);//  长度
            baos.write((byte) 0x01);//  01= 3.1  00 = 2.0 vCard 2.1 or 3.0


            baos.write((byte) 0x04);//maxlistcount 取多少个
            baos.write((byte) 0x02);// 长度

            baos.write((byte) 0xFF);// ffff表示取所有的.
            baos.write((byte) 0xFF);*/

        } catch (IOException e) {
            Log.e(TAG, "", e);
            e.printStackTrace();
        }
        return baos;
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
            /**
             Header Authentication Challenge C1
             Header Authentication Response C2
             Header Field/Header Application Parameters M
             PbapSupportedFeatures C3
             */
            baos.write((byte) 0x80); // Opcode

            baos.write((byte) 0x00); // Packet Length Varies
            baos.write((byte) 0x1A);

            baos.write((byte) 0x10); // OBEX Version Number  1.0
            baos.write((byte) 0x00); // Flags

            baos.write((byte) 0x40); // Maximum Packet Length Varies
            baos.write((byte) 0x06);

            baos.write((byte) 0x46); // Field/Header Application Parameters
            baos.write((byte) 0x00); // length

            baos.write((byte) 0x13); // PbapSupportedFeatures
            /**
             *  The use of the Target header is mandatory in the Phone Book Access Profile.
             *  The UUID below shall be used in the Target header:
             *  796135f0-f0c5-11d8-0966-0800200c9a66
             */
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
            /**
             Header Authentication Challenge C1
             Header Authentication Response C2
             */
            /**
             * Response Code :
             * a0 // Success code
             * 00 // Packet Length
             * 1f
             * 10 // OBEX Version Number
             * 00 // Flags
             * ff //Maximum Packet Length
             * fe
             * cb 00 00 00 01  //Connection ID
             * 4a 00 13  //Header of Who
             * 796135f0f0c511d809660800200c9a66  //Target
             */

            byte[] phoneBookDownloadReq = baos.toByteArray();
            Log.i(TAG, "First send datas = " + ConvertByteUtils.bytesToHexString(phoneBookDownloadReq));
            os.write(phoneBookDownloadReq);
            os.flush();
            Log.i(TAG, "First send OVER.");

            final BluetoothSocket tempSocket = socket;
            TimerTask task = new TimerTask() {
                public void run() {
                    //execute the task
                    try {
                        OutputStream os = tempSocket.getOutputStream();
                        ByteArrayOutputStream baosG = getVcard();
                        byte[] bookDownloadReq = baosG.toByteArray();
                        Log.i(TAG, "Second send datas = " + ConvertByteUtils.bytesToHexString(bookDownloadReq));
                        os.write(bookDownloadReq);
                        os.flush();
                        Log.i(TAG, "Second send OVER.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 5000);


            InputStream is = socket.getInputStream();
            ByteArrayOutputStream baosR = new ByteArrayOutputStream();
            int ch = -1, op;
            byte[] buffer = new byte[2048];
            while ((ch = is.read(buffer)) != -1) {
                baosR.write(buffer, 0, ch);
                byte[] resp = baosR.toByteArray();
                op = resp[0] & BIT_MASK;
                Log.i(TAG, "Get Response Code = " + ConvertByteUtils.bytesToHexString(resp));
            }


        } catch (IOException e) {
            Log.e(TAG, "", e);
            e.printStackTrace();
        } finally {
            Log.i(TAG, "OVER");
        }
    }
}