# GuardHelper

Main Functionalities: User Login, Accept TaskOrder(Text), SignIn, Report Location, Image, Text and Video

Techs:
- implemetned simple NumberPickerDialog
- prevented popping up keyboard on EditText, showing dialog instead
- used OkHttp to work with web services
- XML create and parse: DOM method
- implemented Timer with Handler and Runnable
- save parameters in SharedPreference (data/data/packagename/shared_pref)
- used Baidu Location System to get location
  * calling getPermissions runtime, if not cannot work in 6.0+
- created custome Application class to get activity stack and current activity
- popping up images and videos from chat page (zoomImageFromThumb)
- transferring between Bitmap and Base64 to upload & download image data
- implemented zoom in & out of ImageView (sephiroth74/ImageViewZoom)
- used VideoView to play video, let it be in RelativeLayout to be centered
