package com.rosantos.coc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClanWar extends Clan {
	
	War war;

	Integer attacks;
	
	Integer stars;
	
	Double destructionPercentage;

	@JsonProperty("members")
	List<PlayerWar> membersWar;

	public Integer getAttacks() {
		return attacks;
	}

	public void setAttacks(Integer attacks) {
		this.attacks = attacks;
	}

	public Integer getStars() {
		return stars;
	}

	public void setStars(Integer stars) {
		this.stars = stars;
	}

	public Double getDestructionPercentage() {
		return destructionPercentage;
	}

	public void setDestructionPercentage(Double destructionPercentage) {
		this.destructionPercentage = destructionPercentage;
	}

	public List<PlayerWar> getMembersWar() {
		return membersWar;
	}

	public void setMembersWar(List<PlayerWar> membersWar) {
		this.membersWar = membersWar;
	}

	public War getWar() {
		return war;
	}

	public void setWar(War war) {
		this.war = war;
	}

	@Override
	public String toString() {
		return "ClanWar [attacks=" + attacks + ", stars=" + stars + ", destructionPercentage=" + destructionPercentage
				+ ", membersWar=" + membersWar + ", tag=" + tag + ", name=" + name + ", clanLevel=" + clanLevel
				+ ", members=" + members + "]";
	}
	
}
