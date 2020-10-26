package com.rosantos.coc.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.rosantos.coc.model.Clan;
import com.rosantos.coc.model.ClanLeague;
import com.rosantos.coc.model.ClanWar;
import com.rosantos.coc.model.ConstantsCOC;
import com.rosantos.coc.model.EnumWarState;
import com.rosantos.coc.model.Player;
import com.rosantos.coc.model.PlayerWar;
import com.rosantos.coc.model.Round;
import com.rosantos.coc.model.War;

@Service
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

	public War getWar(Clan clan) {
		try {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_CLAN_CURRENT_WAR, War.class, clan.getTag());
		} catch (HttpClientErrorException e) {
			System.err.println(e.getMessage());
		}
		return null;
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
			playerWar.updateValues(getPlayer(playerWar.getTag()));
		}
	}

	private Player getPlayer(String playerTag) {
		if (!mapPlayer.containsKey(playerTag)) {
			mapPlayer.put(playerTag, playerService.getPlayer(playerTag));
		}
		return mapPlayer.get(playerTag);
	}

	public Clan getClan(String clanTag) {
		Clan clan = this.restTemplate.getForObject(IServicesAPI.SERVICE_CLAN, Clan.class, clanTag);
		List<Player> players = new ArrayList<Player>();
		for (Player member : clan.getMembers()) {
			players.add(getPlayer(member.getTag()));
		}
		clan.setMembers(players);
		return clan;
	}

	public Clan updateClan(War war, Clan clan) {
		if (war == null || EnumWarState.notInWar.equals(war.getState())) {
			return clan;
		}

		ClanWar result = clan.getTag().equals(war.getClan().getTag()) ? war.getClan() : war.getOpponent();
		result.setWar(war);
		updatePlayerValues(result);
		result.setMembers(clan.getMembers());
		List<String> tags = result.getMembersWar().stream().map(PlayerWar::getTag).collect(Collectors.toList());
		List<Player> list = clan.getMembers().stream().filter(player ->tags.contains(player.getTag())).collect(Collectors.toList());
		result.getMembers().removeAll(list);
		result.getMembers().addAll(result.getMembersWar());
		
		return result;
	}

}
