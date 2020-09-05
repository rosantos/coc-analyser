package com.rosantos.coc.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@PropertySource("classpath:application.properties")
public class COCService {
	
	protected RestTemplate restTemplate;
	
	@Autowired
    public COCService(RestTemplateBuilder restTemplateBuilder, @Value("${coc.token}") String token) {
        this.restTemplate = restTemplateBuilder.build();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>() ;
        interceptors.add(new COCClientHttpRequestInterceptor(token));
		restTemplate.setInterceptors(interceptors);
    }

}
