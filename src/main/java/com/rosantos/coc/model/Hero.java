package com.rosantos.coc.model;

public class Hero {
	String name;

	Integer level;

	Integer maxLevel;

	EnumVillage village;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	public EnumVillage getVillage() {
		return village;
	}

	public void setVillage(EnumVillage village) {
		this.village = village;
	}

	public Double getEvolution() {
		return (level.doubleValue() / maxLevel.doubleValue()) * 100;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Hero other = (Hero) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Hero [name=" + name + ", level=" + level + ", maxLevel=" + maxLevel + ", village=" + village
				+ ", evolution=" + getEvolution() + "]";
	}

}
