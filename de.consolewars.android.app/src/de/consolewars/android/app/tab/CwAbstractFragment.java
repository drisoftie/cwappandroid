package de.consolewars.android.app.tab;

import android.support.v4.app.Fragment;

public abstract class CwAbstractFragment extends Fragment {

	private String title;
	private boolean isSelected = false;

	public CwAbstractFragment() {
	}

	public CwAbstractFragment(String title) {
		this.title = title;
		setRetainInstance(true);
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public abstract void backPressed();

	/**
	 * @return the isSelected
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected
	 *            the isSelected to set
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
}