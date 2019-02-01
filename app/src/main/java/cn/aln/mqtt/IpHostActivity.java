package cn.aln.mqtt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class IpHostActivity extends AppCompatActivity {

    private EditText ip;
    private EditText port;
    private EditText sub ,name,psd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_host);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        sub = findViewById(R.id.sub);
        name = findViewById(R.id.name);
        psd = findViewById(R.id.psd);
        Button connect = findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ip.getText().toString().length() !=0 && port.getText().toString().length()!=0 &&
                        sub.getText().toString().length()!=0&&name.getText().toString().length()!=0&&
                        psd.getText().toString().length()!=0){
                    Intent intent = new Intent(IpHostActivity.this,MainActivity.class);
                    intent.putExtra("ip",ip.getText().toString());
                    intent.putExtra("port",port.getText().toString());
                    intent.putExtra("sub",sub.getText().toString());
                    intent.putExtra("name",name.getText().toString());
                    intent.putExtra("psd",psd.getText().toString());
                    startActivity(intent);
                }
            }
        });

    }
}
