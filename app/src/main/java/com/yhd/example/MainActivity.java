package com.yhd.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.yhd.swfplayer.SwfPlayerHelper;


public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitUI();
        initSwf();
    }

    /**
     * 初始化UI
     */
    private void InitUI() {
        webView=(WebView)findViewById(R.id.webView);
    }

    /**
     * 初始化并播放flash
     */
    private void initSwf() {
        //工程assets目录下swf文件对应的html文件路径，如果直接传入swf文件的路径也可以播放，但是不能与js交互
        String assetsPath="file:///android_asset/main.html";
        SwfPlayerHelper.getInstance(getApplicationContext())
                .setJSCallClassName("jsCallClassName")//设置js调用的类名
                .setJSCallMethodName("jsCallMethodName")//设置js调用的方法名
                .setWebView(webView)//设置flash播放的载体
                .setSwfPlayerCallBack(new SwfPlayerHelper.SwfPlayerCallBack() {//设置播放过程的回调

                    @Override
                    public void onCallBack(SwfPlayerHelper.CallBackState state, final Object... args) {
                        Log.v(TAG, state.toString());
                        //收到js调用方法发来的参数字符串信息
                        if(state== SwfPlayerHelper.CallBackState.JS_CALL_ANDROID_METHOD_WITH_PARAM){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),(String)args[0],Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                })
        .playSwf(assetsPath);//传入绝对路径、带file://的绝对路径、url都行
    }

    /**
     * 加载Antivity是调用
     */
    @Override
    protected void onResume() {
        super.onResume();
        SwfPlayerHelper.getInstance(getApplicationContext()).onResume();
    }

    /**
     * Activity停止时调用
     */
    @Override
    protected void onPause() {
        super.onPause();
        SwfPlayerHelper.getInstance(getApplicationContext()).onPause();
    }
}
