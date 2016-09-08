package com.highjump.guardhelper.api;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Administrator on 2016/7/28.
 */
public class ApiResult {

    // 参数名称
    private final String PARAM_ACTION = "action";
    private final String PARAM_RESULT = "result";
    private final String NODE_ROOT = "data";

    private String mstrResult;

    public String getResult() {
        return mstrResult;
    }

    private Element mEleData;

    public ApiResult(String response) {
        try {
            // 返回来的数据的都是encoded, 所以先把它解码
            String strResponseDecoded = URLDecoder.decode(response, "UTF-8");
            resolveParamXML(strResponseDecoded);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析XML格式的字符串数据
     * @param strResponse - 反馈数据
     */
    private void resolveParamXML(String strResponse) {

        String strResponseReal = strResponse.trim();
//        strResponseReal = strResponseReal.replace("\\r", "").replace("\\n", "").replace("\\t", "");

        // 先把str换成inputsource, 不然parse函数把它当成URI
        StringReader stringReader  =  new StringReader(strResponseReal);
        InputSource inputSource  =  new InputSource(stringReader);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputSource);

            doc.getDocumentElement().normalize();

            // data节点
            NodeList nlData = doc.getElementsByTagName(NODE_ROOT);
            mEleData = (Element) nlData.item(0);

            // action节点
            NodeList nlAction = mEleData.getElementsByTagName(PARAM_ACTION);
            Element eleAction = (Element) nlAction.item(0);
            String strAction = eleAction.getChildNodes().item(0).getNodeValue();

            // result节点
            NodeList nlResult = mEleData.getElementsByTagName(PARAM_RESULT);
            Element eleResult = (Element) nlResult.item(0);
            mstrResult = eleResult.getChildNodes().item(0).getNodeValue();

        } catch (ParserConfigurationException e) {      // factory.newDocumentBuilder
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {     // 有可能没有result这个节点，所以设置异常处理
            e.printStackTrace();
        }
    }

    /**
     * 获取返回结果的节点内容
     * @return
     */
    public String getNodeData(String nodeName) {
        String strRes = null;

        if (mEleData == null)
            return null;

        // 节点
        NodeList nlRes = mEleData.getElementsByTagName(nodeName);
        Element eleRes = (Element) nlRes.item(0);

        // 如果找不到这个节点，则退出
        if (eleRes == null) {
            return null;
        }

        strRes = eleRes.getChildNodes().item(0).getNodeValue();

        return strRes;
    }
}
