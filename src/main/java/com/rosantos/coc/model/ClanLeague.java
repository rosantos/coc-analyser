package com.rosantos.coc.model;

import java.util.List;

public class ClanLeague {

    EnumWarState state;

    String season;

    List<Clan> clans;

    List<Round> rounds;

    public EnumWarState getState() {
        return state;
    }

    public void setState(EnumWarState state) {
        this.state = state;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public List<Clan> getClans() {
        return clans;
    }

    public void setClans(List<Clan> clans) {
        this.clans = clans;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    @Override
    public String toString() {
        return "ClanLeague{" +
                "state=" + state +
                ", season='" + season + '\'' +
                ", clans=" + clans +
                ", rounds=" + rounds +
                '}';
    }
}
