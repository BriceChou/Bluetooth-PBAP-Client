package com.bricechou.bluetoothpbapclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

public class SendPbapActivity extends Activity {

    private static final String TAG = SendPbapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pbap);
    }

    protected BluetoothDevice getDevice() {
        //获得BluetoothAdapter对象，该API是android 2.0开始支持的
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        //adapter不等于null，说明本机有蓝牙设备
        if (adapter != null) {
            System.out.println("本机有蓝牙设备！");
            //如果蓝牙设备未开启
            if (!adapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //请求开启蓝牙设备
                startActivity(intent);
            }
            //获得已配对的远程蓝牙设备的集合
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            if (devices.size() > 0) {
                for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                    BluetoothDevice device = (BluetoothDevice) it.next();
                    System.out.println(device.getAddress());
                    return device;
                }
                //打印出远程蓝牙设备的物理地址
            } else {
                System.out.println("还没有已配对的远程蓝牙设备！");
            }
        } else {
            System.out.println("本机没有蓝牙设备！");
        }
        return null;
    }

    private void getVcard(BluetoothSocket socket) {
        try {
            OutputStream os = socket.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            /**
             * Field Opcode GET (0x03 or 0x83) M
             Field Packet Length Varies M
             Header Connection ID Varies M
             Header Single Response Mode 0x01 C1
             Header Single Response Mode Param 0x01 C2
             Header Name Object name (*.vcf) or M
             X-BT-UID (X-BT-UID:*)
             Header Type “x-bt/vcard” M
             Header Application Parameters Varies O
             - PropertySelector Varies O
             - Format
             C1: The Single Response Mode header is mandatory in the first packet if GOEP2.0 or later is used else excluded
             (X).
             C2: The Single Response Mode Param header is optional if Single Response Mode is used else excluded (X).
             */

            baos.write((byte) 0x83);//Get Header 0x83
            baos.write((byte) 0x00);
            baos.write((byte) 0x4F);
            baos.write((byte) 0xCB);//Connection ID   0xCB
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x01);
            baos.write((byte) 0x01);//Name
            baos.write((byte) 0x00);
            baos.write((byte) 0x21);
            char[] cs = new char[]{'*', '.', 'v', 'c', 'f'};//要转换的char数组
            String str = new String(cs);
            byte[] bs = str.getBytes("Unicode");
            baos.write(bs);
            baos.write((byte) 0x42);//Type
            baos.write((byte) 0x00);
            baos.write((byte) 0x12);
            byte[] typeBytes = "x-bt/vcard".getBytes("Unicode");
            short typeLength = (short) (typeBytes.length + 1 + 3);
            byte[] typeLengthBytes = ConvertByteUtils.shortToBytes(typeLength);
            baos.write(typeLengthBytes);//type长度2字节
            baos.write(typeBytes);//type name
            baos.write((byte) 0x00);//type name 结束
            baos.write((byte) 0x4C);//AppParam
            baos.write((byte) 0x00);//appParam length
            baos.write((byte) 0x14);
            baos.write((byte) 0x06);//Tag for Filter
            baos.write((byte) 0x08);//length field
            baos.write((byte) 0x00);//不对vcard做任何过滤
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x00);
            baos.write((byte) 0x07);//Tag for Format
            baos.write((byte) 0x01);//length field
            baos.write((byte) 0x01);//format value vcard 3.0
            baos.write((byte) 0x04);//Tag for MLC
            baos.write((byte) 0x02);//length
            baos.write((byte) 0xff);//0xff
            baos.write((byte) 0xff);//0xff
            byte[] phoneBookDownloadReq = baos.toByteArray();
            Log.i(TAG, "Send datas = " + ConvertByteUtils.bytesToHexString(phoneBookDownloadReq));
            os.write(phoneBookDownloadReq);
            os.flush();

            InputStream isG = socket.getInputStream();
            int chG = -1;
            byte[] bufferG = new byte[2048];
            while ((chG = isG.read(bufferG)) != -1) {
                baos.write(bufferG, 0, chG);
                byte[] respG = baos.toByteArray();
                Log.i(TAG, "Get Response Code = " + ConvertByteUtils.bytesToHexString(respG));

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
