package com.rosantos.coc.services;

public interface IServicesAPI {

	String API_ROOT = "https://api.clashofclans.com/v1";

	String TAG_CLAN = "clanTag";
	String TAG_WAR = "warTag";
	String TAG_PLAYER = "playerTag";
	
	String SERVICE_CLAN_CURRENT_WAR = API_ROOT+"/clans/{"+TAG_CLAN+"}/currentwar";
	String SERVICE_CLAN_CURRENT_LEAGUE = API_ROOT+"/clans/{"+TAG_CLAN+"}/currentwar/leaguegroup";
	String SERVICE_CLAN = API_ROOT+"/clans/{"+TAG_CLAN+"}";
	String SERVICE_CLAN_WAR_LEAGUE = API_ROOT+"/clanwarleagues/wars/{"+TAG_WAR+"}";
	String SERVICE_CLAN_WAR_LOG = API_ROOT+"/clans/{"+TAG_CLAN+"}/warlog";
	String SERVICE_CLAN_MEMBERS = API_ROOT+"/clans/{"+TAG_CLAN+"}/members";
	String SERVICE_CLANS = API_ROOT+"/clans";
	
	String SERVICE_PLAYER = API_ROOT+"/players/{"+TAG_PLAYER+"}";

}
