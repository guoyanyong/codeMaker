package com.cloud.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/28 11:39
 */
public class HttpsUtils {
    public static String serverUrl = "http://192.168.201.254:8811";
//    public static String serverUrl = "http://localhost:8811";
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 300000;
    private static final Logger logger = LoggerFactory.getLogger(HttpsUtils.class);
    static {
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        // Validate connections after 1 sec of inactivity
        connMgr.setValidateAfterInactivity(1000);
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        requestConfig = configBuilder.build();
    }
    /**
     * 发送 GET 请求（HTTP），不带输入数据	 * 	 *
     * @param url	 *
     * @return
     */
    public static JSONObject doGet(String url) {
        return doGet(url, new HashMap<String, Object>(), new HashMap<String, Object>());
    }
    /**
     * 发送 GET 请求（HTTP），K-V形式	 * 	 *
     * @param url	 *
     * @param params	 *
     * @return
     */
    public static JSONObject doGet(String url, Map<String, Object> params,  Map<String, Object> headers){
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        HttpClient httpClient = getCloseableHttpClient(apiUrl);
        try {
            HttpGet httpGet = new HttpGet(apiUrl);
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue().toString());
            }
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = IOUtils.toString(instream, "UTF-8");
            }
            return new JSONObject(result);
        } catch (Exception e) {
            showErrorMessage(e);
        }
        return null;
    }

    private static void showErrorMessage(Exception e) {
        StringBuffer errorSb = new StringBuffer();
        if (e.getCause()!=null){
            errorSb.append(e.getCause()).append("\n");
        }
        if (e.getMessage()!=null){
            errorSb.append(e.getMessage()).append("\n");
        }
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            errorSb.append(stackTraceElement).append("\n");
        }
        Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "错误提示", errorSb.toString(), NotificationType.ERROR));
    }

    /**
     * 发送 POST 请求（HTTP），
     * 不带输入数据	 * 	 *
     * @param apiUrl	 *
     * @return
     */
    public static JSONObject doPost(String apiUrl) {
        return doPost(apiUrl, new HashMap<String, Object>());
    }
    /**
     * 发送 POST 请求，K-V形式	 * 	 *
     * @param apiUrl	 *            API接口URL	 *
     * @param params	 *            参数map	 *
     * @return
     */
    public static JSONObject doPost(String apiUrl, Map<String, Object> params, Map<String, Object> headers) {
        CloseableHttpClient httpClient = getCloseableHttpClient(apiUrl);
        CloseableHttpResponse response = null;
        try {
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setConfig(requestConfig);
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                pairList.add(pair);
            }
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue().toString());
            }
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
            response = httpClient.execute(httpPost);

            return getResponse(response);

        } catch (Exception e) {
            showErrorMessage(e);
        } finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
	                logger.error(e.getMessage());
                }
            }
        }
        return null;
    }
    /**
     * 发送 POST 请求，JSON形式
     * @param apiUrl
     * @param json json对象
     * @return
     */
    public static JSONObject doPost(String apiUrl, Object json) {
        CloseableHttpClient httpClient = getCloseableHttpClient(apiUrl);
        HttpPost httpPost = new HttpPost(apiUrl);
        CloseableHttpResponse response = null;
        try {
            httpPost.setConfig(requestConfig);
            StringEntity stringEntity = new StringEntity(json.toString(), "UTF-8");
            // 解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            response = httpClient.execute(httpPost);

            return getResponse(response);
        } catch (IOException e) {
            showErrorMessage(e);
        }  finally {
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                	logger.error(e.getMessage());
                }
            }
        }
        return null;
    }

    private static JSONObject getResponse(CloseableHttpResponse response) throws IOException {
        String httpStr;
        HttpEntity entity = response.getEntity();
        httpStr = EntityUtils.toString(entity, "UTF-8");
        System.out.println(response.getStatusLine());
        for (Header allHeader : response.getAllHeaders()) {
            System.out.println(allHeader.getName()+":"+allHeader.getValue());
        }
        System.out.println(httpStr);

        try {
            JSONObject jsonObject = new JSONObject(httpStr);
            Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "操作提示", "请求成功", NotificationType.INFORMATION));
            return jsonObject;
        } catch (JSONException e) {
            showErrorMessage(e);
        }
        return null;
    }

    private static CloseableHttpClient getCloseableHttpClient(String apiUrl) {
        CloseableHttpClient httpClient = null;
        if (apiUrl.startsWith("https")) {
            httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory())
                    .setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        } else {
            httpClient = HttpClients.createDefault();
        }
        return httpClient;
    }

    /**
     * 创建SSL安全连接
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (GeneralSecurityException e) {
        	logger.error(e.getMessage());
        }
        return sslsf;
    }
}