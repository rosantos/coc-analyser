package com.rosantos.coc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerWar extends Player implements Comparable<PlayerWar>{

	Integer mapPosition;
	
	List<WarAttack> attacks;
	
	Integer opponentAttacks;
	
	@JsonProperty("townhallLevel")
	protected Integer townHallLevel;
	
	WarAttack bestOpponentAttack;

	public Integer getMapPosition() {
		return mapPosition;
	}

	public void setMapPosition(Integer mapPosition) {
		this.mapPosition = mapPosition;
	}

	public List<WarAttack> getAttacks() {
		return attacks;
	}

	public void setAttacks(List<WarAttack> attacks) {
		this.attacks = attacks;
	}

	public Integer getOpponentAttacks() {
		return opponentAttacks;
	}

	public void setOpponentAttacks(Integer opponentAttacks) {
		this.opponentAttacks = opponentAttacks;
	}

	public WarAttack getBestOpponentAttack() {
		return bestOpponentAttack;
	}

	public void setBestOpponentAttack(WarAttack bestOpponentAttack) {
		this.bestOpponentAttack = bestOpponentAttack;
	}

	@Override
	public String toString() {
		return "PlayerWar [mapPosition=" + mapPosition + ", attacks=" + attacks + ", opponentAttacks=" + opponentAttacks
				+ ", townHallLevel=" + townHallLevel + ", bestOpponentAttack=" + bestOpponentAttack + ", tag=" + tag
				+ ", name=" + name + ", townHallWeaponLevel=" + townHallWeaponLevel + ", expLevel=" + expLevel
				+ ", role=" + role + ", heroes=" + heroes + "]";
	}

	public void updateValues(Player player) {
		setExpLevel(player.getExpLevel());
		setHeroes(player.getHeroes());
		setName(player.getName());
		setRole(player.getRole());
		setTag(player.getTag());
		setTownHallLevel(player.getTownHallLevel());
		setTownHallWeaponLevel(player.getTownHallWeaponLevel());
	}

	@Override
	public int compareTo(PlayerWar o) {
		return this.getMapPosition().compareTo(o.getMapPosition());
	}
}
