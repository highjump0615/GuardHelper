package com.highjump.guardhelper.api;

import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.model.UserData;
import com.highjump.guardhelper.utils.CommonUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

/**
 * Created by Administrator on 2016/7/26.
 */
public class API_Manager {

    private static String API_PATH = "http://192.168.1.113/guardhelper/";
    private static String API_PATH_DATA = "http://192.168.1.113/guardhelper/";
    private static String API_PATH_ORDER = "http://192.168.1.113/guardhelper/";

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
//    private int mnTimeout = 60000;

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
        API_PATH = path;
        API_PATH_ORDER = path;
    }


    public static void setApiPath(String urlData, String urlOrder) {
        API_PATH = urlOrder;
        API_PATH_DATA = urlData;
        API_PATH_ORDER = urlOrder;
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
                          AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_LOGIN);
        mapParam.put("username", username);
        mapParam.put("password", CommonUtils.getMD5EncryptedString(userpassword));

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * 上报数据
     * @param user - 用户对象
     * @param data - 数据
     */
    public void reportData(UserData user,
                           ReportData data,
                           AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_REPORTDATA);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.mTencentGPSTracker.getLongitude() + "," +CommonUtils.mTencentGPSTracker.getLatitude());
        mapParam.put("time", data.getTime());
        mapParam.put("information", data.getData());
        mapParam.put("datatype", data.getType());

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * 上报位置
     * @param user - 用户对象
     */
    public void reportLocation(UserData user,
                               AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_REPORTLOCATION);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.mTencentGPSTracker.getLongitude() + "," +CommonUtils.mTencentGPSTracker.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * 请求命令
     * @param user - 用户对象
     * @param responseHandler
     */
    public void queryOrder(UserData user,
                           AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_QUERYORDER);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.mTencentGPSTracker.getLongitude() + "," +CommonUtils.mTencentGPSTracker.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * 获取命令
     * @param user - 用户对象
     * @param orderNo - 命令编号
     * @param responseHandler
     */
    public void getOrder(UserData user,
                         String orderNo,
                         AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_GETORDER);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.mTencentGPSTracker.getLongitude() + "," +CommonUtils.mTencentGPSTracker.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());
        mapParam.put("orderno", orderNo);

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * 到达签到
     * @param user - 用户对象
     * @param responseHandler
     */
    public void signArrival(UserData user,
                            AsyncHttpResponseHandler responseHandler) {

        RequestParams params = new RequestParams();

        // 创建参数Map
        Map<String, Object> mapParam = new HashMap<String, Object>();
        mapParam.put(PARAM_ACTION, ACTION_SIGN);
        mapParam.put("username", user.getUsername());
        mapParam.put("location", CommonUtils.mTencentGPSTracker.getLongitude() + "," + CommonUtils.mTencentGPSTracker.getLatitude());
        mapParam.put("time", getCurrentTimeFormat());

        sendToServiceByPost(API_PATH, params, mapParam, responseHandler);
    }

    /**
     * POST方式发送请求
     * @param serviceAPIURL - API Url
     * @param params - 参数
     */
    private void sendToServiceByPost(String serviceAPIURL,
                                     RequestParams params,
                                     Map<String, Object> mapParam,
                                     AsyncHttpResponseHandler responseHandler) {

        try {
            // encode XML数据
            String strDataEncoded = URLEncoder.encode(createParamXML(mapParam), "UTF-8");
            params.put("data", strDataEncoded);

            AsyncHttpClient client = new AsyncHttpClient();

//            client.setTimeout(mnTimeout);
//            client.setResponseTimeout(mnTimeout);
//            client.setConnectTimeout(mnTimeout);

            client.post(serviceAPIURL, params, responseHandler);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
