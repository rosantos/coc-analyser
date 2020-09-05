package com.rosantos.coc.model;

import java.util.List;
import java.util.Objects;

public class Player {
	String tag;

	String name;

	Integer townHallWeaponLevel;

	Integer expLevel;

	String role;

	List<Hero> heroes;

	protected Integer townHallLevel;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getTownHallLevel() {
		return townHallLevel;
	}

	public void setTownHallLevel(Integer townHallLevel) {
		this.townHallLevel = townHallLevel;
	}

	public Integer getTownHallWeaponLevel() {
		return townHallWeaponLevel;
	}

	public void setTownHallWeaponLevel(Integer townHallWeaponLevel) {
		this.townHallWeaponLevel = townHallWeaponLevel;
	}

	public Integer getExpLevel() {
		return expLevel;
	}

	public void setExpLevel(Integer expLevel) {
		this.expLevel = expLevel;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<Hero> getHeroes() {
		return heroes;
	}

	public void setHeroes(List<Hero> heroes) {
		this.heroes = heroes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Player member = (Player) o;
		return tag.equals(member.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}

	@Override
	public String toString() {
		return "Player [tag=" + tag + ", name=" + name + ", townHallWeaponLevel=" + townHallWeaponLevel + ", expLevel="
				+ expLevel + ", role=" + role + ", heroes=" + heroes + ", townHallLevel=" + townHallLevel + "]";
	}

}
