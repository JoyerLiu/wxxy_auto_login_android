package top.joyer.wxxy_auto_login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SuccussActivity extends AppCompatActivity {

    Button buttonUnbind;
    TextView textViewSno;
    TextView textViewNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_succuss);

        buttonUnbind=findViewById(R.id.buttonUnbind);
        textViewNet=findViewById(R.id.textViewNet);
        textViewSno=findViewById(R.id.textViewSno);
        textViewSno.setText("学号："+getIntent().getStringExtra("sno"));
        textViewNet.setText("登录的网络："+getIntent().getStringExtra("net"));


        buttonUnbind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 解绑
                AlertDialog alertDialog1 = new AlertDialog.Builder(SuccussActivity.this)
                        .setTitle("确认操作")//标题
                        .setMessage("确定要注销登录吗？")//内容
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                unbind();
                            }
                        })
                        .setNegativeButton("取消",null)
                        .create();
                alertDialog1.show();
            }
        });
    }



    private JSONObject getJSONByStream(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonpResponse=sb.toString();
            // 查找第一个括号和最后一个括号的位置
            int startIndex = jsonpResponse.indexOf("(") + 1;
            int endIndex = jsonpResponse.lastIndexOf(")");

            // 从响应中提取JSON字符串
            if (startIndex > 0 && endIndex > startIndex) {
                String json = jsonpResponse.substring(startIndex, endIndex);
                return new JSONObject(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void unbind(){
        String sno=getIntent().getStringExtra("sno");
        new Thread(){
            @Override
            public void run() {
                super.run();
                HttpURLConnection connection=null;
                try {
                    URL url = new URL(
                            "http://10.1.99.100:801/eportal/portal/mac/unbind?" +
                                    "callback=dr1002" +
                                    "&user_account=" + sno +
                                    "&wlan_user_mac=" +
                                    "&wlan_user_ip=" +
                                    "&jsVersion=4.1.3" +
                                    "&v=526" +
                                    "&lang=zh");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setDoOutput(false);
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code" + responseCode);
                    }
                    JSONObject result = getJSONByStream(connection.getInputStream());

                    String msg=result.get("msg").toString();
                    SuccussActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(msg.equals("解绑终端MAC成功！") || msg.indexOf("获取用户在线信息数据为空！")!=-1){
                                Intent intent=new Intent(SuccussActivity.this,MainActivity.class);
                                intent.putExtra("isUnbind",true);
                                Toast.makeText(SuccussActivity.this, "注销登录成功", Toast.LENGTH_SHORT).show();
                                startActivity(intent); //进入登录页面
                                finish();
                            }else{
                                AlertDialog alertDialog1 = new AlertDialog.Builder(SuccussActivity.this)
                                        .setTitle("注销登录失败")//标题
                                        .setMessage("错误："+msg)//内容
                                        .setPositiveButton("确定",null)
                                        .create();
                                alertDialog1.show();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    SuccussActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog1 = new AlertDialog.Builder(SuccussActivity.this)
                                    .setTitle("注销登录失败")//标题
                                    .setMessage("错误："+ e.toString())//内容
                                    .setPositiveButton("确定",null)
                                    .create();
                            alertDialog1.show();
                        }
                    });
                }
            }
        }.start();
    }
}