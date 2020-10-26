package com.rosantos.coc.model;

import java.util.List;
import java.util.Objects;

public class Player {
	protected String tag;

	protected String name;

	protected Integer townHallWeaponLevel;

	protected Integer expLevel;

	protected String role;

	protected List<Hero> heroes;

	protected Integer bestTrophies;

	protected Integer trophies;

	protected League league;

	protected Integer donations;

	protected Integer donationsReceived;

	protected Integer attackWins;

	protected Integer townHallLevel;

	protected List<PlayerAchievementProgress> achievements;

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

	public Integer getBestTrophies() {
		return bestTrophies;
	}

	public void setBestTrophies(Integer bestTrophies) {
		this.bestTrophies = bestTrophies;
	}

	public Integer getTrophies() {
		return trophies;
	}

	public void setTrophies(Integer trophies) {
		this.trophies = trophies;
	}

	public Integer getDonations() {
		return donations;
	}

	public void setDonations(Integer donations) {
		this.donations = donations;
	}

	public Integer getDonationsReceived() {
		return donationsReceived;
	}

	public void setDonationsReceived(Integer donationsReceived) {
		this.donationsReceived = donationsReceived;
	}

	public Integer getAttackWins() {
		return attackWins;
	}

	public void setAttackWins(Integer attackWins) {
		this.attackWins = attackWins;
	}

	public List<PlayerAchievementProgress> getAchievements() {
		return achievements;
	}

	public void setAchievements(List<PlayerAchievementProgress> achievements) {
		this.achievements = achievements;
	}

	public Integer progressValue(String progressName) {
		if (achievements != null) {
			PlayerAchievementProgress progress = achievements.stream()
					.filter(item -> item.getName().equalsIgnoreCase(progressName)).findFirst().get();
			if (progress != null) {
				return progress.getValue();
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || !(o instanceof Player) || getClass() != o.getClass())
			return false;
		Player member = (Player) o;
		return tag.equals(member.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}

	public League getLeague() {
		return league;
	}

	public void setLeague(League league) {
		this.league = league;
	}

	@Override
	public String toString() {
		return "Player [tag=" + tag + ", name=" + name + ", townHallWeaponLevel=" + townHallWeaponLevel + ", expLevel="
				+ expLevel + ", role=" + role + ", heroes=" + heroes + ", townHallLevel=" + townHallLevel + "]";
	}

	public String getSmallImage() {
		return getLeague() != null && getLeague().getIconUrls() != null ? getLeague().getIconUrls().small : null;
	}

}
