package org.ssdev.WettkampfManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Result {
	private final StringProperty rank;
	private final StringProperty name;
	private final StringProperty table;
	private final StringProperty seating;
	private final StringProperty time;
	
	public Result(String rank, String name, String table, String seating, String time) {
		this.rank = new SimpleStringProperty(rank);
		this.name = new SimpleStringProperty(name);
		this.table = new SimpleStringProperty(table);
		this.seating = new SimpleStringProperty(seating);
		this.time = new SimpleStringProperty(time);
	}
	
	public StringProperty getRankProperty() {
		return rank;
	}

	public StringProperty getNameProperty() {
		return name;
	}

	public StringProperty getTableProperty() {
		return table;
	}

	public StringProperty getSeatingProperty() {
		return seating;
	}

	public StringProperty getTimeProperty() {
		return time;
	}
}
