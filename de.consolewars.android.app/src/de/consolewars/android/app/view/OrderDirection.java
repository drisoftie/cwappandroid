package de.consolewars.android.app.view;

/**
 * 
 * 
 * @author Alexander Dridiger
 */
public class OrderDirection {

	String[] columns;
	int[] direction;
	String defaultOrder = "";

	public OrderDirection(String[] columns) {
		this.columns = columns;
		direction = new int[this.columns.length];
		for (int i = 0; i < direction.length; i++) {
			direction[i] = 0;
		}
	}

	public String getOrder(String column) {
		String orderby = "";
		for (int i = 0; i < columns.length; i++) {
			if (column.toLowerCase().matches(columns[i])) {
				if (direction[i] == 0) {
					direction[i] = 1;
					return " " + columns[i] + " asc";
				} else {
					direction[i] = 0;
					return " " + columns[i] + " desc";
				}
			}
		}	
		return orderby;
	}
}
