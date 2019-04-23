package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.event.SubscribeEvent;
import com.kedacom.confinterface.restclient.mcu.InspectionSrcInfo;
import com.kedacom.confinterface.restclient.mcu.JoinConfFailInfo;
import com.kedacom.confinterface.restclient.mcu.JoinConfFailMt;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.client.transport.LongPollingTransport;
import org.cometd.websocket.client.JettyWebSocketTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.net.*;
import java.util.*;

@ConditionalOnProperty(name = "confinterface.sys.useMcu", havingValue = "true", matchIfMissing = true)
@Service
public class McuSubscribeClientService implements ApplicationContextAware{

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void initializing(String srvIpIn) {
        handShakeOk = false;
        srvIp = srvIpIn;
        channelList = Collections.synchronizedList(new ArrayList<>());
        msglistener = new MsgListener();
        sublistener = new SubListener();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    public boolean handshake(List<String> cookies) {
        System.out.println("now start hand shake..............");

        HttpClient httpclient = new HttpClient();
        try {
            httpclient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ClientTransport httpTransport = new LongPollingTransport(null, httpclient);
        ClientSessionChannel.MessageListener handShakeListener = new HshakeListener();

        String url = "http://" + srvIp + "/api/v1/publish";
        WebSocketClient webSocketClient = new WebSocketClient();
        try {
            webSocketClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            JettyWebSocketTransport wsTransport = new JettyWebSocketTransport(null, null, webSocketClient);
            bayeuxClient = new BayeuxClient(url, wsTransport, httpTransport);

            CookieManager handle = (CookieManager) CookieHandler.getDefault();
            CookieStore store = handle.getCookieStore();
            List<HttpCookie> lstCookie = store.getCookies();
            if (null == lstCookie || lstCookie.isEmpty()){
                System.out.println("getCookies null, input cookie :" + cookies);

                if (null != cookies && !cookies.isEmpty()) {
                    for (String cookie : cookies) {
                        String[] keyValue = cookie.split("=");
                        System.out.println("handshake, cookie:"+cookie+", keyvalue[0]:"+keyValue[0]+", keyvalue[1]:"+keyValue[1]);
                        HttpCookie httpCookie = new HttpCookie(keyValue[0], keyValue[1]);
                        bayeuxClient.putCookie(httpCookie);
                    }
                }

            } else {
                bayeuxClient.putCookie(lstCookie.get(0));
            }
            bayeuxClient.handshake(handShakeListener);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void subscribe(String channel) {
        String subChannel = userDomain + channel;
        bayeuxClient.getChannel(subChannel).subscribe(msglistener, sublistener);
    }

    public void unsubscribe(String channel) {
        String subChannel = userDomain + channel;
        channelList.remove(subChannel);
        bayeuxClient.getChannel(subChannel).unsubscribe();
    }

    public boolean isHandShakeOk() {
        return handShakeOk;
    }

    //握手回调
    private class HshakeListener implements ClientSessionChannel.MessageListener {
        public void onMessage(ClientSessionChannel channel, Message message) {
            if (message.isSuccessful()) {
                handShakeOk = true;
                //握手成功，获取domain_id
                Map<String, Object> map = message.getExt();
                domainId = map.get("user_domain_moid").toString();
                System.out.println("handshake ok, user domain id: " + domainId);
                userDomain = "/userdomains/" + domainId;

                for (String subChannel : channelList) {
                    bayeuxClient.getChannel(subChannel).subscribe(msglistener, sublistener);
                }

            } else {
                System.out.println("handshake failed!");
                handShakeOk = false;
            }
        }
    }

    //订阅是否成功
    private class SubListener implements ClientSessionChannel.MessageListener {
        @Override
        public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {
            if (message.isSuccessful()) {
                //获取订阅的通道并保存
                String strChannel = message.get("subscription").toString();
                channelList.add(strChannel);

                System.out.println("Subsription successful! channel:"+strChannel);
            } else {
                System.out.println("Subsription failed!");
            }
        }
    }

    //订阅消息内容
    private class MsgListener implements ClientSessionChannel.MessageListener {

        @Override
        public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {
            System.out.println("subcontent: " + message.getJSON());

            //获取订阅返回的通道消息
            String strChannel = message.getChannel();
            Map<String, Object> data = message.getDataAsMap();
            String method = data.get("method").toString();
            int errorCode = 0;
            Object errorCodeObj = data.get("error_code");
            if (null != errorCodeObj){
                errorCode = Integer.valueOf(errorCodeObj.toString());
            }

            //获取第三个“/”后面的内容,去掉域信息的通道
            //eg:/userdomains/xxx/xxx/xxx/xxx
            String subChannel = strChannel.substring(userDomain.length());
            String[] subString = subChannel.split("/", 4);
            String confId = subString[2];
            System.out.println("subscribe channel: " + strChannel + ", channel: " + subChannel + ", confId: "+ confId + ", method: " + method + ", errCode: "+errorCode);

            if (subChannel.contains("mts")){
                if (subChannel.contains("cascades")) {
                    System.out.println("publishEvent, confId:"+confId+", method:"+method+", channel:"+subChannel);
                    SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, null);
                    applicationContext.publishEvent(subscribeEvent);
                } else {
                    JoinConfFailInfo joinConfFailInfo = new JoinConfFailInfo();
                    JoinConfFailMt joinConfFailMt = new JoinConfFailMt();
                    Map<String, Object> mtInfo = (Map<String, Object>) data.get("mt");
                    joinConfFailMt.setAccount(mtInfo.get("account").toString());
                    joinConfFailMt.setAccount_type(Integer.valueOf(mtInfo.get("account_type").toString()));
                    joinConfFailInfo.setJoinConfFailMt(joinConfFailMt);
                    if (20423 == errorCode) {
                        String occupyConfName = data.get("occupy_confname").toString();
                        System.out.println("occupyConfName : " + occupyConfName);
                        joinConfFailInfo.setOccupy_confname(occupyConfName);
                    } else {
                        joinConfFailInfo.setOccupy_confname(null);
                    }

                    System.out.println("publish event, errCode :"+errorCode+", account_type :"+joinConfFailMt.getAccount_type()+", account:"+joinConfFailMt.getAccount());
                    SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, joinConfFailInfo);
                    applicationContext.publishEvent(subscribeEvent);
                }
            } else if (subChannel.contains("inspections")){
                //选看通道 /confs/{conf_id}/inspections/{mt_id}/{mode}
                if (0 != errorCode){
                    //选看失败
                    Map<String, Object> inspectSrc = (Map<String, Object>) data.get("src");
                    InspectionSrcInfo inspectionSrcInfo = new InspectionSrcInfo();
                    inspectionSrcInfo.setType(Integer.valueOf(inspectSrc.get("type").toString()));
                    inspectionSrcInfo.setMt_id(inspectSrc.get("mt_id").toString());

                    SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, inspectionSrcInfo);
                    applicationContext.publishEvent(subscribeEvent);
                } else {
                    //选看成功
                    SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, null);
                    applicationContext.publishEvent(subscribeEvent);
                }
            } else if (subChannel.contains("speaker")){
                //设置发言人
                SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, null);
                applicationContext.publishEvent(subscribeEvent);
            } else {
                System.out.println("other channel : " + strChannel);
                SubscribeEvent subscribeEvent = new SubscribeEvent(this, confId, method, errorCode, subChannel, null);
                applicationContext.publishEvent(subscribeEvent);
            }
        }
    }

    private ApplicationContext applicationContext = null;

    private static boolean handShakeOk;
    private static String srvIp;
    private static String domainId;
    private static String userDomain;
    private static BayeuxClient bayeuxClient;
    private static List<String> channelList;     //通道列表，用来存已订阅的通道
    private static ClientSessionChannel.MessageListener msglistener; //回调订阅内容
    private static ClientSessionChannel.MessageListener sublistener; //回调订阅是否成功
}
