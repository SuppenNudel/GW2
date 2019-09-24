package me.xhsun.guildwars2wrapper.model.v2.quests;

import java.util.List;

public class Quest {
	
	private Integer id;
	private String name;
	private Integer level;
	private String story;
	private List<Goal> goals;
	
	public Integer getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Integer getLevel() {
		return level;
	}
	public String getStory() {
		return story;
	}
	public List<Goal> getGoals() {
		return goals;
	}

}
