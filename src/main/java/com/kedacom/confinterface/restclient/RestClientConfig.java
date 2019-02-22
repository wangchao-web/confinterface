package com.kedacom.confinterface.restclient;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfig {
    @Value("${confinterface.http.maxTotal:500}")
    private int maxTotal;

    @Value("${confinterface.http.defaultMaxPerRoute:200}")
    private int defaultMaxPerRoute;

    @Value("${confinterface.http.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${confinterface.http.connectionRequestTimeout:10000}")
    private int connectionRequestTimeout;

    @Value("${confinterface.http.socketTimeout:10000}")
    private int socketTimeout;

    @Value("${confinterface.http.requestRetry:3}")
    private int requestRetry;

    @Override
    public String toString() {
        return new StringBuilder().append("maxTotal:").append(maxTotal)
                .append(",defaultMaxPerRoute:").append(defaultMaxPerRoute)
                .append(",connectTimeout:").append(connectTimeout)
                .append(",connectionRequestTimeout:").append(connectionRequestTimeout)
                .append(",socketTimeout:").append(socketTimeout)
                .append(",requestRetry:").append(requestRetry)
                .toString();
    }

    @Bean
    public HttpClient httpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true).build();

            //在此处使用了NoopHostnameVerifier策略，即关闭主机名验证，接受任何有效的SSL会话来匹配目标主机
            //另外一种策略为DefaultHostnameVerifier，HttpClient使用的默认实现,与RFC2818兼容
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
            connectionManager.setMaxTotal(maxTotal);
            connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout)
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .build();

            return HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLContext(sslContext)
                    .setConnectionManager(connectionManager)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(requestRetry, true))
                    .build();
        }catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        return HttpClients.createDefault();
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.ALL);
        byteArrayHttpMessageConverter.setSupportedMediaTypes(mediaTypeList);
        restTemplate.getMessageConverters().add(byteArrayHttpMessageConverter);

        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypeList);
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

        return restTemplate;
    }
}
