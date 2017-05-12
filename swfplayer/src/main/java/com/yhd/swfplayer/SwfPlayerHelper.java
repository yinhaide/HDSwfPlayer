package com.yhd.swfplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class SwfPlayerHelper {
	private static final String TAG = "SwfPlayerHelper";
	/** flash信任目录头 */
	private static final String FlashPlayerTrustFileHead="/data/data";
	/** flash信任目录尾 */
	private static final String FlashPlayerTrustFileTail="/app_plugins/com.adobe.flashplayer/.macromedia/Flash_Player/#Security/FlashPlayerTrust";
	/** flash信任文件名 */
	private static final String FlashPlayerTrustFileName="/idealtrust";
	private static Context mContext;
	private static String AdobeTrustFilePath="";
	private static SwfPlayerHelper instance;
	private Holder uiHolder;
	private String result=null;
	private SwfPlayerCallBack mSwfPlayerCallBack;
	private String jsCallClassName="jsCallClassName";
	private String jsCallMethodName="jsCallMethodName";

	/** 状态枚举 */
	public enum CallBackState{
		JS_CALL_ANDROID_METHOD_WITH_PARAM("js调用android的方法并传来参数"),
		JS_CALL_ANDROID_METHOD_WITHOUT_PARAM("js调用android的方法"),
		ANDROID_CALL_JS_METHOD_WITH_RETURM("android调用js的方法并传来返回值"),
		SDK_VERSION_TOO_HIGHT("当前版本太该，可能不支持flash的播放"),
		RECEIVER_ERRO("播放Flash出现异常"),
		PAGE_START("WebView开始加载"),
		PAGE_FINISH("WebView加载完成"),
		WEBVIEW_NULL("没有设置WebView"),
		CREATE_FLASH_TRUST_FILE_FAIL("创建flash信任路径失败"),
		FLASHPLAYER_NO_EXIST("flashplayer插件不存在");

		private final String state;

		CallBackState(String var3) {
			this.state = var3;
		}

		public String toString() {
			return this.state;
		}
	}

	/**
	 * 获得静态类
	 * @param context 对象
	 * @return 类对象
	 */
	public static synchronized SwfPlayerHelper getInstance(Context context){
		mContext=context;
		AdobeTrustFilePath=FlashPlayerTrustFileHead+ File.separator+mContext.getPackageName()+FlashPlayerTrustFileTail;
		if(instance == null){
			instance=new SwfPlayerHelper();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	public SwfPlayerHelper() {
		this.uiHolder = new Holder ();
	}

	/**
	 * 设置flash播放的webview载体，必须要设置
	 * @param mwebView mwebView
	 */
	public SwfPlayerHelper setWebView(WebView mwebView){
		this.uiHolder.webView=mwebView;
		InitWebView();
		return instance;
	}

	/**
	 * JS调用Android的类名
	 * @param className className
	 */
	public SwfPlayerHelper setJSCallClassName(String className){
		this.jsCallClassName=className;
		return instance;
	}

	/**
	 * JS调用Android的方法名
	 * @param methodName methodName
	 */
	public SwfPlayerHelper setJSCallMethodName(String methodName){
		this.jsCallMethodName=methodName;
		return instance;
	}
	
	/**
	 * 设置播放flash过程回调，必须要设置
	 * @param mSwfPlayerCallBack 回调类
	 */
	public SwfPlayerHelper setSwfPlayerCallBack(SwfPlayerCallBack mSwfPlayerCallBack){
		this.mSwfPlayerCallBack=mSwfPlayerCallBack;
		return instance;
	}
	
	/**
	 * 判断flash插件是否存在
	 * @return 插件是否存在
	 */
	private boolean CheckFlash() {  
       PackageManager pm = mContext.getPackageManager();
       List<PackageInfo> infoList = pm.getInstalledPackages(PackageManager.GET_SERVICES);
       for (PackageInfo info : infoList) {
           if ("com.adobe.flashplayer".equals(info.packageName)) {  
               return true;  
           }  
       }  
       return false;  
	}
	
	/**
	 * 模拟点击一次屏幕，防止flash焦点丢失导致不能与js交互
	 */
	@SuppressLint("Recycle")
	private void PerformClick(){
		this.uiHolder.webView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, this.uiHolder.webView.getLeft() + 5,this.uiHolder.webView.getTop() + 5, 0));
		this.uiHolder.webView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,this.uiHolder.webView.getLeft() + 5, this.uiHolder.webView.getTop() + 5, 0));
	}
	
	/**
	 * 第一次调用初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void InitWebView() {

		//重写Flash加载主要函数
		this.uiHolder.webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
            //Flash启东时调用
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				if(mSwfPlayerCallBack!=null) {
					mSwfPlayerCallBack.onCallBack(CallBackState.PAGE_START, view, url, favicon);
				}
			}
            //Flash加载完成之后调用
			@Override
			public void  onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if(mSwfPlayerCallBack!=null) {
					mSwfPlayerCallBack.onCallBack(CallBackState.PAGE_FINISH, view, url);
				}
				PerformClick();
			}
			//自定义加载失败
			@Override
			public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
			    super.onReceivedError(view, errorCode, description, failingUrl);
				if(mSwfPlayerCallBack!=null) {
					mSwfPlayerCallBack.onCallBack(CallBackState.RECEIVER_ERRO, view, errorCode, description, failingUrl);
				}
			}
		});
		//给网页文件添加Android与JS交互函数的定义,jsCallClassName为网页调用的类JSCallAndroidClass的别名
		this.uiHolder.webView.addJavascriptInterface(new JSCallAndroidClass(), jsCallClassName);//暂时屏蔽，因为还没交互需求
		//完成配置滞后加载flash
		this.uiHolder.settings = this.uiHolder.webView.getSettings();
		//设置允许与js交互
		this.uiHolder.settings.setJavaScriptEnabled(true);
		//设置允许文件操作
		this.uiHolder.settings.setAllowFileAccess(true);
		//设置允许使用Adobe Flash播放视频
		this.uiHolder.settings.setPluginState(PluginState.ON);
		//设置加载方式是替换加载，而不是新页面加载
		this.uiHolder.settings.setLoadWithOverviewMode(true);
		//设置编码方式
		this.uiHolder.settings.setDefaultTextEncodingName("UTF-8");
		//设置透明背景
		this.uiHolder.webView.setBackgroundColor(Color.rgb(0,0,0));
		//重写Flash加载辅助函数
		this.uiHolder.webView.setWebChromeClient(new WebChromeClient() {
			
			public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
				super.onShowCustomView(view, callback);
				if ((Build.VERSION.SDK_INT >= 14)){
					if(mSwfPlayerCallBack!=null) {
						mSwfPlayerCallBack.onCallBack(CallBackState.SDK_VERSION_TOO_HIGHT);
					}
				}
			}
			@Override
			public void onHideCustomView() {
				super.onHideCustomView();
			}
		});
		//设置网页能够获得焦点
		this.uiHolder.webView.requestFocusFromTouch();
		//如果不设置，则在点击网页文本输入框时，不能弹出软键盘及不响应其他的一些事件。
		this.uiHolder.webView.requestFocus();
		this.uiHolder.webView.setFocusable(true);
	}
	
	/**
     * 创建flash播放信任文件，实现与js交互
     * @param path 文件路径
     * @param name 文件名
     * @param message 内容
     */
    private void createAdobeTrustFileWithString(String path, String name, String message) {
    	File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		File filePath = new File(path+name);
		if(filePath.exists()){
			filePath.delete();
		}
    	try {
			filePath.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        try { 
            FileOutputStream fout = new FileOutputStream(path+name);
            byte[] bytes = message.getBytes();  
            fout.write(bytes);  
            fout.close(); 
        } catch (Exception e) {
            e.printStackTrace();
			if(mSwfPlayerCallBack!=null) {
				mSwfPlayerCallBack.onCallBack(CallBackState.CREATE_FLASH_TRUST_FILE_FAIL);
			}
        }  
    }

	/**
	 * 播放flash
	 * @param mswfPath 文件路径,要以"file://"开头
	 */
	public boolean playSwf(String mswfPath){
		String finalPath;
		if(!mswfPath.contains("file://")){
			finalPath="file://"+mswfPath;
		}else{
			finalPath=mswfPath;
		}
		if(uiHolder.webView==null){
			if(mSwfPlayerCallBack!=null) {
				mSwfPlayerCallBack.onCallBack(CallBackState.WEBVIEW_NULL);
			}
			return false;
		}
		if(CheckFlash()){
			createAdobeTrustFileWithString(AdobeTrustFilePath,FlashPlayerTrustFileName,mswfPath);
			uiHolder.webView.loadUrl(finalPath);
		}else{
			if(mSwfPlayerCallBack!=null) {
				mSwfPlayerCallBack.onCallBack(CallBackState.FLASHPLAYER_NO_EXIST);
			}
		}
		return true;
	}

	/**
	 * Android调用JS的方法与JS交互，如果Android需要与Flash交互的话
	 * Android在4.4之前不能调用JS的有返回值函数
	 * JS再次调用Flash的ActionScript的方法即可
	 * WebView调用js的基本格式为:webView.loadUrl(“javascript:methodName(parameterValues)”)
	 * @param methodString JS的类加方法字符串
	 */
	public void androidCallJsMethod(String methodString){
		uiHolder.webView.loadUrl(methodString);
	}

	/**
	 * Android调用JS的方法与JS交互，如果Android需要与Flash交互的话
	 * Android 4.4之后使用evaluateJavascript可以获得JS函数的返回值
	 * JS再次调用Flash的ActionScript的方法即可
	 * WebView调用js的基本格式为:webView.loadUrl(“javascript:methodName(parameterValues)”)
	 * evaluateJavascript方法必须在UI线程（主线程）调用
	 * @param methodString JS的类加方法字符串
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public void androidCallJSMethodWithReturn(String methodString){
		uiHolder.webView.evaluateJavascript(methodString, new ValueCallback<String>() {

			@Override
			public void onReceiveValue(String value) {
				result=value;
				if(mSwfPlayerCallBack!=null) {
					mSwfPlayerCallBack.onCallBack(CallBackState.ANDROID_CALL_JS_METHOD_WITH_RETURM, result);
				}
			}});
	}

	/**
	 * 为了让退出播放或者在播放时用户转到其它页面后flash不再播放，应该重写用于播放的Activity的onResume方法，
	 * 并分别调用webview的隐藏方法onResume
	 */
	public void onResume() {
		try {
			PerformClick();
			uiHolder.webView.getClass().getMethod("onResume").invoke(uiHolder.webView, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * 为了让退出播放或者在播放时用户转到其它页面后flash不再播放，应该重写用于播放的Activity的onPause方法，
	 * 并分别调用webview的隐藏方法onPause
	 */
	public void onPause() {
		try {
			uiHolder.webView.getClass().getMethod("onPause").invoke(uiHolder.webView, (Object[]) null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ui容器
	 */
	public static final class Holder {
		public WebView webView;
		public WebSettings settings;
	}
	
	/**
	 * 用来接收js数据的类
	 */
	public class JSCallAndroidClass{

		/**
		 * html文件中的js代码调用该函数给Android传值
		 * @param str 命令字符串
		 */
		public void jsCallMethodName(String str) {
			if(mSwfPlayerCallBack!=null) {
				mSwfPlayerCallBack.onCallBack(CallBackState.JS_CALL_ANDROID_METHOD_WITH_PARAM, str);
			}
		}

		/**
		 * html文件中的js代码调用该函数
		 */
		public void jsCallMethodName() {
			if(mSwfPlayerCallBack!=null) {
				mSwfPlayerCallBack.onCallBack(CallBackState.JS_CALL_ANDROID_METHOD_WITHOUT_PARAM);
			}
		}

	}
	
	/**
	 * 回调接口
	 */
	public interface SwfPlayerCallBack {
		/**
		 * 状态回调
		 * @param state 状态ID
		 * @param args 若干参数
		 */
		public void onCallBack(CallBackState state, Object... args);
	}
}
