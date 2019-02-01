package cn.aln.mqtt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private MyMqttService mqttService;
    private EditText input;
    private Button warn;
    private TextView rece;
    private String imei;
    private String ip;
    private String port;
    private String sub ,name ,psd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rece = (TextView) findViewById(R.id.rece);
        input = (EditText) findViewById(R.id.input);
        warn = findViewById(R.id.warn);

        psd = getIntent().getStringExtra("psd");
        name = getIntent().getStringExtra("name");
        sub = getIntent().getStringExtra("sub");
        ip = getIntent().getStringExtra("ip");
        port = getIntent().getStringExtra("port");
        warn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    //消息内容
                    String msg = input.getText().toString();
                    if (msg.length()== 0){
                        Toast.makeText(MainActivity.this, "empty msg!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //消息主题
                    String topic = sub;
                    //消息策略
                    int qos = 0;
                    //是否保留
                    boolean retained = false;
                    //发布消息
                    publish(msg, topic, qos, retained);
                    input.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
        GetIMEI();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @SuppressLint("HardwareIds")
    private void GetIMEI() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //申请CAMERA权限
            Log.d(TAG, "********没有权限 去申请");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
        } else {
            Log.d(TAG, "*********** 已经有权限了");
            TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                imei = mTelephony.getDeviceId();
            } else {
                imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            Log.d(TAG,"imei "+ imei);
            if (imei!=null) {
                buildEasyMqttService();
                connect();
            }
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == 2) {
            Log.d(TAG, "*********** 同意开启相机");
            //IMEI（imei）
            TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null) {
                imei = mTelephony.getDeviceId();
            } else {
                //android.provider.Settings;
                imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            Log.d(TAG,"imei "+ imei);
            buildEasyMqttService();
            connect();
        } else {
            Toast.makeText(this, "您拒绝了开启相机功能", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "***********");
        }

    }




    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    /**
     * 判断服务是否连接
     */
    private boolean isConnected() {
        return mqttService.isConnected();
    }

    /**
     * 发布消息
     */
    private void publish(String msg, String topic, int qos, boolean retained) {
        mqttService.publish(msg, topic, qos, retained);
    }

    /**
     * 断开连接
     */
    private void disconnect() {
        mqttService.disconnect();
    }

    /**
     * 关闭连接
     */
    private void close() {
        mqttService.close();
    }

    /**
     * 订阅主题
     */
    private void subscribe() {
        String[] topics = new String[]{sub};
        //主题对应的推送策略 分别是0, 1, 2 建议服务端和客户端配置的主题一致
        // 0 表示只会发送一次推送消息 收到不收到都不关心
        // 1 保证能收到消息，但不一定只收到一条
        // 2 保证收到切只能收到一条消息
        int[] qoss = new int[]{0};
        mqttService.subscribe(topics, qoss);
    }

    /**
     * 连接Mqtt服务器
     */
    private void connect() {
        mqttService.connect(new IEasyMqttCallBack() {
            @Override
            public void messageArrived(String topic, String message, int qos) {
                //推送消息到达
                Log.e(TAG, "message= " + message);
                rece.setText("收到消息:" + message +'\n' +"主题--> " + topic);

            }
            @Override
            public void connectionLost(Throwable arg0) {
                //连接断开
                try {
                    Log.e(TAG + "connectionLost", arg0.toString());
                    Toast.makeText(MainActivity.this, "connectionLost", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {

                } finally {

                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                Log.e(TAG + "@deliveryComplete", "发送完毕" + arg0.toString());
                Toast.makeText(MainActivity.this, "deliveryComplete", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void connectSuccess(IMqttToken arg0) {
                Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_LONG).show();
                Log.e(TAG + "@@@@@connectSuccess", "success");
                subscribe();
            }
            @Override
            public void connectFailed(IMqttToken arg0, Throwable arg1) {
                //连接失败
                Log.e(TAG + "@@@@@connectFailed", "fail" + arg1.getMessage());
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 构建EasyMqttService对象
     */
    private void buildEasyMqttService() {
        mqttService = new MyMqttService.Builder()
                //设置自动重连
                .autoReconnect(true)
                //设置清除回话session  true(false) 不收(收)到服务器之前发出的推送消息
                .cleanSession(true)
                //唯一标示 保证每个设备都唯一就可以 建议 imei
                .clientId(imei)
                .userName(name)
                .passWord(psd)
                //mqtt服务器地址 格式例如：
                //  tcp://iot.eclipse.org:1883
                .serverUrl("tcp://"+ip+":"+port)
                //心跳包默认的发送间隔
                .keepAliveInterval(20)
                .timeOut(10)
                //构建出EasyMqttService 建议用application的context
                .bulid(this.getApplicationContext());
    }
}