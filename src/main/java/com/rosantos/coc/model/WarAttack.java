package com.rosantos.coc.model;

public class WarAttack implements Comparable<WarAttack> {
	String attackerTag;
	String defenderTag;
	
	Integer stars;
	Double destructionPercentage;
	Integer order;
	public String getAttackerTag() {
		return attackerTag;
	}
	public void setAttackerTag(String attackerTag) {
		this.attackerTag = attackerTag;
	}
	public String getDefenderTag() {
		return defenderTag;
	}
	public void setDefenderTag(String defenderTag) {
		this.defenderTag = defenderTag;
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
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	
	@Override
	public String toString() {
		return "WarAttack [attackerTag=" + attackerTag + ", defenderTag=" + defenderTag + ", stars=" + stars
				+ ", destructionPercentage=" + destructionPercentage + ", order=" + order + "]";
	}
	@Override
	public int compareTo(WarAttack o) {
		return this.getOrder().compareTo(o.getOrder());
	}
	
}
