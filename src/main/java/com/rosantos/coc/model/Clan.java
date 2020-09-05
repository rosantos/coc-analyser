package com.rosantos.coc.model;

import java.util.List;
import java.util.Objects;

public class Clan {
    String tag;

    String name;

    Integer clanLevel;

    List<Player> members;

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

    public Integer getClanLevel() {
        return clanLevel;
    }

    public void setClanLevel(Integer clanLevel) {
        this.clanLevel = clanLevel;
    }

    public List<Player> getMembers() {
        return members;
    }

    public void setMembers(List<Player> members) {
        this.members = members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clan clan = (Clan) o;
        return tag.equals(clan.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public String toString() {
        return "Clan{" +
                "tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", clanLevel=" + clanLevel +
                ", members=" + members +
                '}';
    }
}
