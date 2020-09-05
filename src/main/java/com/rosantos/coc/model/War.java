package com.rosantos.coc.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class War {

	EnumWarState state;
	
	Integer teamSize;
	
	ClanWar clan;
	
	ClanWar opponent;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyyMMdd'T'HHmmss")
	Date preparationStartTime;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyyMMdd'T'HHmmss")
	Date startTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyyMMdd'T'HHmmss")
	Date endTime;
	
	public EnumWarState getState() {
		return state;
	}

	public void setState(EnumWarState state) {
		this.state = state;
	}

	public Integer getTeamSize() {
		return teamSize;
	}

	public void setTeamSize(Integer teamSize) {
		this.teamSize = teamSize;
	}

	public ClanWar getClan() {
		return clan;
	}

	public void setClan(ClanWar clan) {
		this.clan = clan;
	}

	public ClanWar getOpponent() {
		return opponent;
	}

	public void setOpponent(ClanWar oponent) {
		this.opponent = oponent;
	}

	public Date getPreparationStartTime() {
		return preparationStartTime;
	}

	public void setPreparationStartTime(Date preparationStartTime) {
		this.preparationStartTime = preparationStartTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clan == null) ? 0 : clan.hashCode());
		result = prime * result + ((opponent == null) ? 0 : opponent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		War other = (War) obj;
		if (clan == null) {
			if (other.clan != null)
				return false;
		} else if (!clan.equals(other.clan))
			return false;
		if (opponent == null) {
			if (other.opponent != null)
				return false;
		} else if (!opponent.equals(other.opponent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "War [state=" + state + ", teamSize=" + teamSize + ", clan=" + clan + ", opponent=" + opponent + "]";
	}
	
	
}
