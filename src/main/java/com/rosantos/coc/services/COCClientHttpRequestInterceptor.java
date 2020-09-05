package com.rosantos.coc.services;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class COCClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private String token;	
	
	public COCClientHttpRequestInterceptor(String token) {
		super();
		this.token = token;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		request.getHeaders().setBearerAuth(token);
		return execution.execute(request, body);
	}

}
