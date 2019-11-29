# HDSwfPlayer
[![Platform](https://img.shields.io/badge/平台-%20Android%20-brightgreen.svg)](https://github.com/yinhaide/HDBluetooth/wiki)
[![characteristic](https://img.shields.io/badge/特点-%20轻量级%20%7C%20简单易用%20%20%7C%20稳定%20-brightgreen.svg)](https://github.com/yinhaide/HDBluetooth/wiki)
> 谷歌中国API链接:https://developer.android.google.cn <br/>
> 支持swf播放以及html带swf的播放。 <br/>
> 支持swf与js的交互。 <br/>
> 自动写入flash信任路径。 <br/>
> 提供播放回调。 <br/>
> Android版本不要超过4.3。 <br/>
> 需要安装flashplayer插件。 <br/>

![](https://github.com/yinhaide/HDSwfPlayer/raw/master/resource/swfplayer.gif)

## 目录
* [如何导入到项目](#Import)
* [如何使用](#Use)
* [关于我](#About)
* [License](#License)

<a name="Import"></a>
### 如何导入到项目
> 支持jcenter方式导入。 <br/>
> 支持本地Module方式导入。 <br/>

#### jcenter方式导入

* 在需要用到这个库的module中的build.gradle中的dependencies中加入

```
dependencies {
    compile 'com.yhd.hdswfplayer:hdswfplayer:1.0.0'
}
```

#### Module方式导入

* 下载整个工程，将hdmediaplayer拷贝到工程根目录,settings.gradle中加入

```
include ':hdswfplayer'
```

* 在需要用到这个库的module中的build.gradle中的dependencies中加入

```
dependencies {
    compile project(':hdswfplayer')
}
```

<a name="Use"></a>
### 如何使用
> 本类支持播放.swf文件、.html文件（.html可以包裹.swf文件并实现与android的交互）。 <br/>
> 在demo中提供.html文件模板实例，如果需要js与android数据交互，请移步demo参考。 <br/>

#### HDSwfPlayerHelper

* 初始化

```
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
```

* 为了让退出播放或者在播放时用户转到其它页面后flash不再播放，应该重写用于播放的Activity的onPause和onResume方法，并分别调用webview的隐藏方法"onPause"和"onResume

```
@Override
protected void onResume() {
    super.onResume();
    SwfPlayerHelper.getInstance(getApplicationContext()).onResume();
}

@Override
protected void onPause() {
    super.onPause();
    SwfPlayerHelper.getInstance(getApplicationContext()).onPause();
}
```

* 更多的操作

```
//WebView调用js的基本格式为:webView.loadUrl(“javascript:methodName(parameterValues)”)
SwfPlayerHelper.getInstance(getApplicationContext()).androidCallJsMethod("jsMethodString");
SwfPlayerHelper.getInstance(getApplicationContext()).androidCallJSMethodWithReturn("jsMethodString");
```

<a name="About"></a>
### 这个项目会持续更新中... 
> 都看到这里了，如果觉得写的可以或者对你有帮助的话，顺手给个星星点下Star~

### 关于我
+ **Email:** [123302687@qq.com](123302687@qq.com)
+ **Github:** [https://github.com/yinhaide](https://github.com/yinhaide)
+ **简书:** [https://www.jianshu.com/u/33c3dd2ceaa3](https://www.jianshu.com/u/33c3dd2ceaa3)
+ **CSDN:** [https://blog.csdn.net/yinhaide](https://blog.csdn.net/yinhaide)

### License

    Copyright 2017 yinhaide
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.