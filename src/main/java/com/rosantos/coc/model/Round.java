package com.rosantos.coc.model;

import java.util.List;

public class Round {
    List<String> warTags;

    public List<String> getWarTags() {
        return warTags;
    }

    public void setWarTags(List<String> warTags) {
        this.warTags = warTags;
    }

    @Override
    public String toString() {
        return "Round{" +
                "warTags=" + warTags +
                '}';
    }
}
