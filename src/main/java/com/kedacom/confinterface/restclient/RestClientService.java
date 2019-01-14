package com.kedacom.confinterface.restclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
                System.out.println("exchange failed, null == resultEntity!");
                return null;
            }

            if (!resultEntity.getStatusCode().is2xxSuccessful()){
                System.out.println("exchange failed! status : "+resultEntity.getStatusCodeValue());
                return null;
            }

            return resultEntity.getBody();
        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public <T> ResponseEntity<T> exchangeJson(String url, HttpMethod method, Object param, Map<String, ?> args, Class<T> returnType){
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_UTF8.toString());

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

    @Autowired
    private RestTemplate restTemplate;
}
