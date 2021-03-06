package com.NUAA.nuaaclub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditEssayActivity extends AppCompatActivity {

    String token;
    String textContent;
    private Button mSubmit;
    private EditText mText;
    private int flag;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取标志: 1为发贴文essay, 2为发回复reply, 3为发BaseReply, 4为发私信message
        flag = (int)getIntent().getExtras().get("flag");
        if(flag == 1)
            setContentView(R.layout.activity_editessay);
        else if(flag == 2 || flag == 3 || flag == 5)
            setContentView(R.layout.activity_editreply);
        else if(flag == 4 )
            setContentView(R.layout.activity_editmessage);
        mSubmit = (Button)findViewById(R.id.essaySubmit);
        mText=(EditText)findViewById(R.id.textEssay);
        mText.setText("");
        mText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textContent=s.toString();
            }
        });//获取文本
        //为发送按钮设置监听器
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. 创建请求队列
                RequestQueue requestQueue = Volley.newRequestQueue(EditEssayActivity.this);
                //2. 创建post请求
                if(flag == 1)
                    url = "http://"+getResources().getString(R.string.address)+":8080/LoginDemo/submitEssay";//发帖
                else if(flag == 2)
                    url = "http://"+getResources().getString(R.string.address)+":8080/LoginDemo/submitReplyServlet";//发回复
                else if(flag == 3)
                    url = "http://"+getResources().getString(R.string.address)+":8080/LoginDemo/submitBaseReplyServlet";
                else if(flag == 4)
                    url = "http://"+getResources().getString(R.string.address)+":8080/LoginDemo/submitMessageServlet";
                else if(flag == 5)
                    url = "http://"+getResources().getString(R.string.address)+":8080/LoginDemo/submitMessageReplyServlet";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.i("s", s);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(EditEssayActivity.this, "网络似乎不通了", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        //获取日期格式
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        //获取日期
                        Date curDate = new Date(System.currentTimeMillis());
                        //得到用于显示的时间
                        String timeStr = formatter.format(curDate);
                        SimpleDateFormat formatterForName = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String timeStrForName = formatterForName.format(curDate);
                        //得到标识符token
                        token = MainActivity.sharedPreferences.getString("token", "");
                        String ID = MainActivity.sharedPreferences.getString("ID", "");
                        String creator = getRandomID(timeStrForName, token);
                        //初始化共性参数
                        map.put("createDate_New", timeStr);//发送时间(用于存入数据库加快查找速度)
                        map.put("createDate", timeStrForName);//发送时间(用于显示的字符串)
                        map.put("text", textContent);//发送内容
                        map.put("userID", ID);//发送者ID
                        if(flag == 1) {
                            map.put("latestDate_New", timeStr);
                            map.put("essayID", creator + "_" + timeStrForName.substring(0,10));//权宜之计
                            map.put("status", "2");//普通贴子
                            map.put("creator",creator);//发送者匿名ID
                        }
                        else if(flag == 2 || flag == 3) {
                            //如果发的是回复在请求参数中给出帖子ID
                            map.put("essayID",(String)getIntent().getExtras().get("essayID"));
                            //从帖子基本信息中得到贴文创建时间, 算法生成回复者匿名ID
                            String essayCreateDateStr=(String)getIntent().getExtras().get("essayCreateDate");
                            map.put("creator", getRandomID(essayCreateDateStr, token));
                            if(flag == 3)
                                map.put("floor",(String) getIntent().getExtras().get("floor"));
                        }
                        else if(flag == 4) {
                            //私信: 给出两方的ID和匿名ID
                            map.put("fromID",getIntent().getExtras().get("fromID").toString());
                            map.put("toID",getIntent().getExtras().get("toID").toString());
                            map.put("fromCreator",creator);
                            map.put("toCreator",getIntent().getExtras().get("toCreator").toString());
                            map.put("fileName", creator + "_"+ getIntent().getExtras().get("toCreator").toString()
                                    + timeStrForName.substring(5,10));//权宜之计
                        }
                        else if(flag == 5) {
                            //私信: 给出两方的ID和匿名ID
                            map.put("fromID",getIntent().getExtras().get("fromID").toString());
                            map.put("toID",getIntent().getExtras().get("toID").toString());
                            map.put("fromCreator",creator);
                            map.put("fileName", getIntent().getExtras().get("fileName").toString());//权宜之计
                        }
                        return map;
                    }
                };
                //3. 将请求添加入请求队列
                requestQueue.add(stringRequest);

                finish();
//                if(flag==1)
//                {
//                    Intent intent = new Intent(EditEssayActivity.this, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
//                    startActivity(intent);
//                }
//                else
//                {
//                    finish();
//                }
            }
        });
    }

    public String getRandomID(String dateTime,String token)
    {
        Random rand =new Random(25);
        String s = (String.valueOf(rand.nextInt(19))
                + String.valueOf(dateTime.charAt(18))
                + token.charAt(7)
                + (((Integer.valueOf(dateTime.charAt(17))+3)*71)%10)
                + ((Integer.valueOf(dateTime.charAt(18))*4567)%10)
                + (token.charAt(10))
                + "");
        return s;
    }

}
