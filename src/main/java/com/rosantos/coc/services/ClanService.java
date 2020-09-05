package com.rosantos.coc.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.rosantos.coc.model.ClanLeague;
import com.rosantos.coc.model.ClanWar;
import com.rosantos.coc.model.ConstantsCOC;
import com.rosantos.coc.model.Player;
import com.rosantos.coc.model.PlayerWar;
import com.rosantos.coc.model.Round;
import com.rosantos.coc.model.War;

@Service
@PropertySource("classpath:application.properties")
public class ClanService extends COCService {

	Map<String, Player> mapPlayer = new HashMap<String, Player>();

	@Autowired
	PlayerService playerService;

	@Autowired
	public ClanService(RestTemplateBuilder restTemplateBuilder, @Value("${coc.token}") String token) {
		super(restTemplateBuilder, token);
	}

	public ClanLeague getClanLeague(String clanTag) {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_CLAN_CURRENT_LEAGUE, ClanLeague.class, clanTag);
	}

	public War getWarLeague(String warTag) {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_CLAN_WAR_LEAGUE, War.class, warTag);
	}

	public List<War> getWarsLeague(String clanTag, ClanLeague clanLeague) {
		List<War> wars = new ArrayList<War>();
		for (Round round : clanLeague.getRounds()) {
			for (String warTag : round.getWarTags()) {
				if (ConstantsCOC.NULL_TAG.equals(warTag)) {
					continue;
				}
				War war = getWarLeague(warTag);
				if ((clanTag.equals(war.getClan().getTag()) || clanTag.equals(war.getOpponent().getTag()))) {
					updatePlayerValues(war.getClan());
					updatePlayerValues(war.getOpponent());
					wars.add(war);
				}
			}
		}
		return wars;
	}

	private void updatePlayerValues(ClanWar clan) {
		for (PlayerWar playerWar : clan.getMembersWar()) {
			if (!mapPlayer.containsKey(playerWar.getTag())) {
				mapPlayer.put(playerWar.getTag(), playerService.getPlayer(playerWar.getTag()));
			}
			playerWar.updateValues(mapPlayer.get(playerWar.getTag()));
		}
	}

}
