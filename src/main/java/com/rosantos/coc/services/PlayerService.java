package com.rosantos.coc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import com.rosantos.coc.model.Player;

@Service
public class PlayerService extends COCService {

	@Autowired
	public PlayerService(RestTemplateBuilder restTemplateBuilder, @Value("${coc.token}") String token) {
		super(restTemplateBuilder, token);
	}

	public Player getPlayer(String playerTag) {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_PLAYER, Player.class, playerTag);
	}
}
