package com.highjump.guardhelper.api;

import android.util.Log;

import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * Created by Administrator on 2016/7/26.
 */
public class API_Manager {

    private static final String TAG = API_Manager.class.getSimpleName();

    private static String API_PATH_DATA = "http://114.249.243.180:3390/APP_API/App_API.jsp";
    private static String API_PATH_ORDER = "http://114.249.243.180:3390/APP_API/App_API_QueryOrder.jsp";
    private static String API_PATH_LOCATION = "http://114.249.243.180:3390/APP_API/App_API_ReportLocation.jsp";

    // API功能
    private final String ACTION_LOGIN = "login";
    private final String ACTION_REPORTDATA = "reportdata";
    private final String ACTION_QUERYORDER = "queryorder";
    private final String ACTION_GETORDER = "sendorder";
    private final String ACTION_SIGN = "signin";
    private final String ACTION_REPORTLOCATION = "reportlocation";

    // 参数名称
    private final String PARAM_ACTION = "action";
    private final String PARAM_DATA = "data";

    // 时限 (毫秒)
    private int mnTimeout = 20000;

    // 实例； 第一次被调用的时候会设置
    private static API_Manager mInstance = null;

    /**
     * 获取API_Manager实例
     */
    public static API_Manager getInstance() {
        if (mInstance == null) {
            mInstance = new API_Manager();
        }

        return mInstance;
    }

    //
    // get/set 函数
    //
    public static String getDataApiPath() {
        return API_PATH_DATA;
    }

    public static void setDataApiPath(String path) {
        API_PATH_DATA = path;
    }

    public static String getOrderApiPath() {
        return API_PATH_ORDER;
    }

    public static void setOrderApiPath(String path) {
        API_PATH_ORDER = path;
    }

    public static String getLocationApiPath() {
        return API_PATH_LOCATION;
    }

    public static void setLocationApiPath(String path) {
        API_PATH_LOCATION = path;
    }

    public static void setApiPath(String urlData, String urlOrder, String urlLocation) {
        API_PATH_DATA = urlData;
        API_PATH_ORDER = urlOrder;
        API_PATH_LOCATION = urlLocation;
    }

    /**
     * 创建XML格式的字符串数据
     * @param params - 参数列表
     * @return XML字符串
     */
    private String createParamXML(Map<String, Object> params) {
        String strResult = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // 创建data节点
            Element eleData = doc.createElement(PARAM_DATA);
            doc.appendChild(eleData);

            // 获取子节点信息
            for (Map.Entry<String, Object> eParam : params.entrySet()) {
                // key是参数名称
                Element eleParam = doc.createElement(eParam.getKey());
                eleData.appendChild(eleParam);

                // 参数值
                Node nodeVal = doc.createTextNode(eParam.getValue().toString());
                eleParam.appendChild(nodeVal);
            }

            // XML属性
            Properties properties = new Properties();
            properties.setProperty(OutputKeys.INDENT, "yes");
            properties.setProperty(OutputKeys.MEDIA_TYPE, "xml");
            properties.setProperty(OutputKeys.VERSION, "1.0");
            properties.setProperty(OutputKeys.ENCODING, "utf-8");
            properties.setProperty(OutputKeys.METHOD, "xml");
//            properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperties(properties);

            DOMSource domSource = new DOMSource(doc.getDocumentElement());
            OutputStream output = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);

            strResult = output.toString();

        } catch (ParserConfigurationException e) {  // factory.newDocumentBuilder
            e.printStackTrace();
        } catch (TransformerConfigurationException e) { // transformerFactory.newTransformer
            e.printStackTrace();
        } catch (TransformerException e) {  // transformer.transform
            e.printStackTrace();
        }

        return strResult;
    }

    /**
     * 返回现在日期时间
     * @return
     */
    private String getCurrentTimeFormat() {

        // 时间格式化 - 换成北京时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String strTime = format.format(new Date());

        return strTime;
    }

    /**
     * 登录
     * @param username - 警号
     * @param userpassword - 密码
     */
    public void userLogin(String username,
                          String userpassword,
                          Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_LOGIN);
        mapParam.put("username", username);
        mapParam.put("password", CommonUtils.getMD5EncryptedString(userpassword));

        sendToServiceByPost(API_PATH_DATA, mapParam, responseCallback);
    }

    /**
     * 上报数据
     * @param user - 用户对象
     * @param data - 数据
     */
    public void reportData(UserData user,
                           ReportData data,
                           Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_REPORTDATA);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.getLongitude() + "," + CommonUtils.getLatitude());
        mapParam.put("time", data.getTime());

        if (data.getType() == ReportData.REPORT_TEXT) {
            mapParam.put("information", data.getStringData());
        }
        else if (data.getType() == ReportData.REPORT_IMAGE) {
            String strBase64 = CommonUtils.bitmapToBase64(data.getBitmapData());
            mapParam.put("information", strBase64);
        }

        mapParam.put("datatype", data.getType());

        sendToServiceByPost(API_PATH_DATA, mapParam, responseCallback);
    }

    /**
     * 上报位置
     * @param user - 用户对象
     */
    public void reportLocation(UserData user,
                               Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_REPORTLOCATION);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.getLongitude() + "," + CommonUtils.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH_LOCATION, mapParam, responseCallback);

        Log.e(TAG, "reportLocation");
    }

    /**
     * 请求命令
     * @param user - 用户对象
     */
    public void queryOrder(UserData user,
                           Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_QUERYORDER);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.getLongitude() + "," + CommonUtils.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH_ORDER, mapParam, responseCallback);
    }

    /**
     * 获取命令
     * @param user - 用户对象
     * @param orderNo - 命令编号
     */
    public void getOrder(UserData user,
                         String orderNo,
                         Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_GETORDER);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.getLongitude() + "," + CommonUtils.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());
        mapParam.put("orderno", orderNo);

        sendToServiceByPost(API_PATH_DATA, mapParam, responseCallback);
    }

    /**
     * 到达签到
     * @param user - 用户对象
     * @param site - 位置
     * @param floor - 楼层
     */
    public void signArrival(UserData user,
                            int site,
                            int floor,
                            Callback responseCallback) {
        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_SIGN);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.getLongitude() + "," + CommonUtils.getLatitude());
        mapParam.put("site", site);

        if (site == 1) {    // 制高点
            mapParam.put("floor", floor);
        }

        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH_DATA, mapParam, responseCallback);
    }

    /**
     * POST方式发送请求
     * @param serviceAPIURL - API Url
     * @param mapParam - 参数键值数组
     */
    private void sendToServiceByPost(String serviceAPIURL,
                                     Map<String, Object> mapParam,
                                     Callback responseCallback) {

        try {
            // encode XML数据\
            String strDataEncoded = URLEncoder.encode(createParamXML(mapParam), "UTF-8");

            // 构建参数
            FormBody formParam = new FormBody.Builder()
                    .add("data", strDataEncoded)
                    .build();

            Request request = new Request.Builder()
                    .url(serviceAPIURL)
                    .post(formParam)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(responseCallback);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
