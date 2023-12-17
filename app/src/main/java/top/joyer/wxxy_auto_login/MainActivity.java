package top.joyer.wxxy_auto_login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button buttonLogin;
    Spinner spinnerNetworkChoose;
    Switch auto_login;
    EditText editTextSno;
    EditText editTextPsw;

    TextView textViewAbout;

    Map<String,String> network= new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        network.put("中国移动","@cmcc");
        network.put("中国联通","@unicom");
        network.put("中国电信","@telecom");
        network.put("无锡学院","");

        buttonLogin=findViewById(R.id.buttonLogin);
        spinnerNetworkChoose=findViewById(R.id.spinner);
        auto_login=findViewById(R.id.switch1);
        editTextPsw=findViewById(R.id.editTextTextPassword);
        editTextSno=findViewById(R.id.editTextSno);
        textViewAbout=findViewById(R.id.textViewAbout);

        textViewAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("关于")//标题
                        .setMessage("此软件为开源项目，严禁倒卖。\n" +
                                "出现问题请到Github ISSUE 提交。\n" +
                                "by JoyerLiu")//内容
                        .setPositiveButton("确定",null)
                        .setNegativeButton("Github", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("https://www.baidu.com");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        })
                        .create();
                alertDialog1.show();
            }
        });

        auto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auto_login.isChecked());
                Toast.makeText(MainActivity.this, "下一次打开该程序将自动登录", Toast.LENGTH_SHORT).show();
            }
        });

        loadData();

        if(auto_login.isChecked() && !getIntent().getBooleanExtra("isUnbind",false)){
            //自动登录
            if(checkInput()){
                saveData();
                login();
            }
        }

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInput()){
                    saveData();
                    login();
                }
            }
        });

    }
    private boolean checkInput(){
        if(editTextSno.getText().toString().isEmpty()|| editTextPsw.getText().toString().isEmpty()|| spinnerNetworkChoose.getSelectedItem().toString()=="请选择网络"){
            AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                    .setTitle("提示")//标题
                    .setMessage("请检查输入的内容有无空缺")//内容
                    .setPositiveButton("确定",null)
                    .create();
            alertDialog1.show();
            return false;
        }
        return true;
    }

    private void saveData(){
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("sno", editTextSno.getText().toString()); // 账号
        editor.putString("psw", editTextPsw.getText().toString()); // 密码
        editor.putString("net",spinnerNetworkChoose.getSelectedItem().toString()); //网络
        editor.putBoolean("auto_login",auto_login.isChecked()); // 自动登录开关
        editor.apply();
    }

    private void loadData(){
        // 从资源文件中加载数据源
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.network_choose,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SharedPreferences prefs = getSharedPreferences("data", MODE_PRIVATE);
        editTextSno.setText(prefs.getString("sno",""));
        editTextPsw.setText(prefs.getString("psw",""));
        spinnerNetworkChoose.setSelection(adapter.getPosition(prefs.getString("net","请选择网络")));
        auto_login.setChecked(prefs.getBoolean("auto_login",false));
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

    private void login(){
        String sno=editTextSno.getText().toString();
        String psw=editTextPsw.getText().toString();
        new Thread(){
            @Override
            public void run() {
                super.run();
                HttpURLConnection connection=null;
                try {
                    URL url = new URL(
                            "http://10.1.99.100:801/eportal/portal/login?" +
                                    "callback=dr1003" +
                                    "&login_method=1" +
                                    "&user_account=,0," + sno + network.get(spinnerNetworkChoose.getSelectedItem().toString()) +
                                    "&user_password=" + psw +
                                    "&wlan_user_ip=" +
                                    "&wlan_user_ipv6=" +
                                    "&wlan_user_mac=000000000000" +
                                    "&wlan_ac_ip=10.1.1.1" +
                                    "&wlan_ac_name=" +
                                    "&jsVersion=4.1.3" +
                                    "&terminal_type=1" +
                                    "&lang=zh-cn&v=9960" +
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
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(msg.equals("Portal协议认证成功！") || msg.contains("已经在线")){
                                Intent intent =new Intent(MainActivity.this,SuccussActivity.class);
                                intent.putExtra("sno",sno);
                                intent.putExtra("net",spinnerNetworkChoose.getSelectedItem().toString());
                                startActivity(intent); //进入完成页面
                                finish();
                            }else{
                                AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("登录失败")//标题
                                        .setMessage("错误："+msg)//内容
                                        .setPositiveButton("确定",null)
                                        .create();
                                alertDialog1.show();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog1 = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("登录失败")//标题
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