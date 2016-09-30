# 警卫助手

主要功能： 用户登录、接收任务指令（文本）、位置（经纬度）定时回传、签到、回送图片、文字、视频

- 简单实现NumberPickerDialog
- EditText弹出键盘无效，反而弹出对话框
- android:windowSoftInputMode <br />
有关弹出键盘时的屏幕状态
- <strike>利用android-async-http-1.4.9来实现数据上报</strike><br />
(不再维护, 反而Volley, OkHttp, Retrofit)
- 采用OkHttp来实现数据上报
- XML创建解析<br />
DOM, SAX, PULL 三种方式； 其中<br />
用DOM方式实现XML创建和解析
- Handler和Runnable实现Timer，定时调用相关api
- 在SharedPreference保存参数 (data/data/packagename/shared_pref)
- <strike>采用腾讯定位获取位置信息</strike><br />
(有些手机上定位不了, 一加, 三星SCH-I879)
- 采用百度地图定位获取位置<br />
处理运行时获得权限(getPermissions), 不然6.0以上就定位不了
- 自定义Application来控制Activity堆叠和当前的Activity
- 通过Bitmap和Base64的相互转换实现了图片的上传和下载
- 实现微信那样从聊天页面缩放图片和视频 (zoomImageFromThumb函数)
- 发出和接收的图片都保存到文件，将Thumbnail图放在内存里面<br />
根据设置inJustDecodeBounds属性来获取图片大小，计算出inSampleSize<br />
调用Bitmap.createScaledBitmap来进一步得到目标大小的缩略图
- 点Thumbnail显示原图片，采用AsyncTask加载本地图片文件
- 实现ImageView的缩放(sephiroth74/ImageViewZoom)<br />
[http://mvnrepository.com/artifact/it.sephiroth.android.library.imagezoom/imagezoom](http://mvnrepository.com/artifact/it.sephiroth.android.library.imagezoom/imagezoom)
[https://github.com/sephiroth74/ImageViewZoom](https://github.com/sephiroth74/ImageViewZoom)
- 用VideoView实现视频的播放，为了居中放在RelativeLayout里面封装了
- 实现了Parcelable做了Activity之间传数据