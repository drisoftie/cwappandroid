package de.consolewars.android.app.view;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * 
 * @author Alexander Dridiger
 */
public class SelectionAndIdMemory {

	ViewGroup previousSelection = null;
	ViewGroup currentSelection = null;
	Drawable colorSelected = null;
	private int[] ids;

	public SelectionAndIdMemory() {
		previousSelection = null;
		currentSelection = null;
	}

	public boolean isSelected() {
		if (currentSelection != null) {
			return true;
		}
		return false;
	}

	public void setColorSelection(Drawable selectedBg) {
		colorSelected = selectedBg;
	}

	public void setSelection(ViewGroup currentRow, Drawable unselectedBg) {
		if (currentSelection != null) {
			currentSelection.setBackgroundDrawable(unselectedBg);
		}
		// previousSelection = currentSelection;
		currentSelection = currentRow;
		if (currentSelection != null) {
			currentSelection.setBackgroundDrawable(colorSelected);
		}
	}

	public String[] getAllValuesFromRow() {
		if (currentSelection != null) {
			String[] values = new String[currentSelection.getChildCount()];
			for (int i = 0; i < currentSelection.getChildCount(); i++) {
				TextView currentValue = (TextView) currentSelection.getChildAt(i);
				values[i] = (String) currentValue.getText();
			}
			return values;
		} else {
			return null;
		}
	}

	/**
	 * Sets the ids to identify entities in the table.
	 * 
	 * @param ids
	 *            the saved ids
	 */
	public void setIds(int[] ids) {
		this.ids = ids;
	}

	public int[] getIds() {
		return ids;
	}

	public int getSelectedRowNumber() {
		return currentSelection.getId() - 1;
	}
}
