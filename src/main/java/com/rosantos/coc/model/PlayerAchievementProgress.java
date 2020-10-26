package com.rosantos.coc.model;

public class PlayerAchievementProgress {
	Integer stars;
	Integer value;
	String name;
	Integer target;
	String info;
	String completionInfo;
	EnumVillage village;
	public Integer getStars() {
		return stars;
	}
	public void setStars(Integer stars) {
		this.stars = stars;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getTarget() {
		return target;
	}
	public void setTarget(Integer target) {
		this.target = target;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getCompletionInfo() {
		return completionInfo;
	}
	public void setCompletionInfo(String completionInfo) {
		this.completionInfo = completionInfo;
	}
	public EnumVillage getVillage() {
		return village;
	}
	public void setVillage(EnumVillage village) {
		this.village = village;
	}
	
}
