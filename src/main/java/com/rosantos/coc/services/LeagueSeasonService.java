package com.rosantos.coc.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import com.rosantos.coc.model.ConstantsCOC;
import com.rosantos.coc.model.League;
import com.rosantos.coc.model.LeagueList;
import com.rosantos.coc.model.LeagueSeason;
import com.rosantos.coc.model.LeagueSeasonList;

@Service
public class LeagueSeasonService extends COCService {

	private static SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
	private LeagueSeason currentSeason;
	
	@Autowired
	public LeagueSeasonService(RestTemplateBuilder restTemplateBuilder, @Value("${coc.token}") String token) {
		super(restTemplateBuilder, token);
	}

	public LeagueList getLeagues() {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_LEAGUES, LeagueList.class);
	}

	public LeagueSeasonList getSeasons(League league) {
		return this.restTemplate.getForObject(IServicesAPI.SERVICE_LEAGUE_SEASON, LeagueSeasonList.class, league.getId());
	}
	
	public String getCurrentSeason() {
		if (currentSeason != null) {
			return calcCurrentSeason(currentSeason);
		}
		LeagueList leagues = getLeagues();
		if (leagues == null) {
			return sdfMonth.format(Calendar.getInstance().getTime());
		}
		
		League leagueLegend = leagues.getItems().stream().filter(league -> ConstantsCOC.LEAGUE_LEGEND.toLowerCase().equalsIgnoreCase(league.getName())).findFirst().get();
		LeagueSeasonList seasonList = getSeasons(leagueLegend);
		if (seasonList== null) {
			return sdfMonth.format(Calendar.getInstance().getTime());
		}
		
		List<LeagueSeason> seasons = seasonList.getItems();
		if (seasons== null || seasons.isEmpty()) {
			return sdfMonth.format(Calendar.getInstance().getTime());
		}
		Collections.sort(seasons,new Comparator<LeagueSeason>() {
			@Override
			public int compare(LeagueSeason o1, LeagueSeason o2) {
				return o2.getId().compareToIgnoreCase(o1.getId());
			}
		});
		currentSeason = seasons.get(0);
		
		return calcCurrentSeason(currentSeason);
		
	}

	private String calcCurrentSeason(LeagueSeason season) {
		try {
			Date date = sdfMonth.parse(season.getId());
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.MONTH, 1);
			return sdfMonth.format(cal.getTime());
		} catch (ParseException e) {
		}
		return sdfMonth.format(Calendar.getInstance().getTime());
	}
}
