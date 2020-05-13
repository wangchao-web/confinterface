package com.kedacom.confinterface.restclient;

import com.kedacom.confinterface.LogService.LogOutputTypeEnum;
import com.kedacom.confinterface.LogService.LogTools;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RestClientService {

    public <T> ResponseEntity<T> postForEntity(String url, Object param, MediaType mediaType, Class<T> returnType) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(mediaType);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
            HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
            return restTemplate.postForEntity(url, httpEntity, returnType);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public <T> T exchange(String url, HttpMethod method, Object param, MediaType mediaType, Class<T> returnType) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(mediaType);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
            HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
            ResponseEntity<T> resultEntity = restTemplate.exchange(url, method, httpEntity, returnType);
            return resultEntity.getBody();

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public <T> T exchange(String url, HttpMethod method, Object param, MediaType mediaType, Map<String, ?> args, Class<T> returnType) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(mediaType);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
            HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
            ResponseEntity<T> resultEntity = restTemplate.exchange(url, method, httpEntity, returnType, args);
            if (null == resultEntity){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"exchange failed, null == resultEntity!");
                System.out.println("exchange failed, null == resultEntity!");
                return null;
            }

            if (!resultEntity.getStatusCode().is2xxSuccessful()){
                LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"exchange failed! status : "+resultEntity.getStatusCodeValue());
                System.out.println("exchange failed! status : "+resultEntity.getStatusCodeValue());
                return null;
            }

            return resultEntity.getBody();
        } catch (Exception e){
            LogTools.info(LogOutputTypeEnum.LOG_OUTPUT_TYPE_FILE,"e.printStackTrace() "+ e.getMessage());
            System.out.println("e.printStackTrace() "+ e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public <T> ResponseEntity<T> exchangeJson(String url, HttpMethod method, Object param, Map<String, ?> args, Class<T> returnType){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());
            //httpHeaders.setConnection("Close");
            HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
            if (null != args) {
                return restTemplate.exchange(url, method, httpEntity, returnType, args);
            } else {
                return restTemplate.exchange(url, method, httpEntity, returnType);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static RestTemplate initRestTemplate() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        //poolingHttpClientConnectionManager.setMaxTotal(2700); // 最大连接数2700
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(50); // 同路由并发数50
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
        //httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)); // 重试次数
        HttpClient httpClient = httpClientBuilder.build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        clientHttpRequestFactory.setConnectTimeout(10000);
        //clientHttpRequestFactory.setConnectionRequestTimeout(20000);
        clientHttpRequestFactory.setReadTimeout(20000);
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        /*List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        MediaType[] mediaTypes = new MediaType[]{
                MediaType.ALL,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.APPLICATION_JSON_UTF8,
                MediaType.APPLICATION_PDF,
        };
        converter.setSupportedMediaTypes(Arrays.asList(mediaTypes));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);*/
        return restTemplate;
    }

    @Autowired
    private RestTemplate restTemplate;


}
