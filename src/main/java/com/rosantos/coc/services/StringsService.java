package com.rosantos.coc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class StringsService {

	public static final String PLAYER_TAG = "header.player.tag";
	public static final String PLAYER_NAME = "header.player.name";
	public static final String PLAYER_XP = "header.player.xp";
	public static final String PLAYER_IN = "header.player.init.season";
	public static final String PLAYER_OUT = "header.player.out.season";
	public static final String PLAYER_TH = "header.player.th";
	public static final String PLAYER_BEST_TROPHIES = "header.player.besttrophies";
	public static final String PLAYER_LEAGUE = "header.player.league";
	public static final String PLAYER_LEAGUE_OK = "header.player.league.ok";
	public static final String PLAYER_TROPHIES = "header.player.trophies";
	public static final String PLAYER_KING = "header.player.king";
	public static final String PLAYER_QUEEN = "header.player.queen";
	public static final String PLAYER_WARDEN = "header.player.warden";
	public static final String PLAYER_CHAMPION = "header.player.champion";
	public static final String PLAYER_DONATIONS = "header.player.donations";
	public static final String PLAYER_ATTACKS = "header.player.attackwins";
	public static final String PLAYER_WAR_DAY = "header.player.war.day";
//	public static final String PLAYER_WAR_ATTACKS = "header.player.war.attacks";
	public static final String PLAYER_CLAN_GAMES = "header.player.clangames";
	public static final String PLAYER_CLAN_GAMES_INIT = "header.player.clangames.init";
	public static final String SCORE_ATTACK = "header.player.score.attack";
	public static final String SCORE_DONATIONS = "header.player.score.donations";
	public static final String SCORE_CLAN_GAMES = "header.player.score.clangames";
	public static final String SCORE_WAR_ATTACKS = "header.player.score.warattacks";
	public static final String SCORE_LEAGUE = "header.player.score.league";
	public static final String SCORE_TOTAL = "header.player.score.total";
	public static final String UPDATED = "updated";
	public static final String PLAYER_PUSH_DATE = "header.player.push.date";
	
	
	@Autowired
	Environment enviroment;

	@Autowired
	public StringsService() {
	}

	public String getMessage(String string) {
		return getMessage(string, null);
	}
	
	public String getMessage(String string, Object... parameters) {
		String result = enviroment.getProperty(string);
		if (parameters != null) {
			result = String.format(result, parameters);
		}
		return result;
	}
}
