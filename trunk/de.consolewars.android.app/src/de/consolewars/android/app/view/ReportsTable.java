package de.consolewars.android.app.view;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TableLayout;
import de.consolewars.android.app.db.AppDataHandler;

/**
 * 
 * 
 * @author Alexander Dridiger
 */
public class ReportsTable {

	private TableLayout tableLayout;
	private Context gridContext;
	final private SelectionAndIdMemory selectionMemory = new SelectionAndIdMemory();
	private String selection, groupBy, having, orderBy = null;
	String[] columns, selectionArgs, labels;
	private OrderDirection order = null;
	private ImageView nameSortIcon, dateSortIcon, scoreSortIcon;
	private AppDataHandler dataHandler;

	public ReportsTable(TableLayout tablegrid, Context context, AppDataHandler dataHandler) {
		tableLayout = (TableLayout) tablegrid;
		gridContext = (Context) context;
		this.dataHandler = dataHandler;
	}

	public void setHeader() {
		// this.labels = gridContext.getResources().getStringArray(R.array.summariesTableColumnNames);
		this.headerCreation();
	}

	private void headerCreation() {
		// View header = (View) LayoutInflater.from(gridContext).inflate(R.layout.reports_header_layout, null);
		// nameSortIcon = (ImageView) header.findViewById(R.id.reports_header_name_sort_icon);
		// dateSortIcon = (ImageView) header.findViewById(R.id.reports_header_date_sort_icon);
		// scoreSortIcon = (ImageView) header.findViewById(R.id.reports_header_score_sort_icon);
		// for (int i = 0; i < labels.length; i++) {
		// if (labels[i].toLowerCase().matches("name")) {
		// setHeaderInfo(labels[i], (TextView) header.findViewById(R.id.reports_header_name),
		// (ViewGroup) header.findViewById(R.id.reports_header_name_layout), "name");
		// nameSortIcon.setImageResource(R.drawable.sort_down);
		// } else if (labels[i].toLowerCase().matches("datum")) {
		// setHeaderInfo(labels[i], (TextView) header.findViewById(R.id.reports_header_date),
		// (ViewGroup) header.findViewById(R.id.reports_header_date_layout), "date");
		// } else if (labels[i].toLowerCase().matches("score")) {
		// setHeaderInfo(labels[i], (TextView) header.findViewById(R.id.reports_header_score),
		// (ViewGroup) header.findViewById(R.id.reports_header_score_layout), "score");
		// }
		// }
		// tableLayout.addView(header);
	}

	//
	// private void setHeaderInfo(String info, TextView header, ViewGroup headerCell, String tag) {
	// header.setText(info);
	// headerCell.setTag(tag);
	// headerCell.setClickable(true);
	// headerCell.setOnClickListener(new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// ViewGroup layout = (ViewGroup) v;
	// if (((String) layout.getTag()).matches("name")) {
	// nameSortIcon = (ImageView) tableLayout.findViewById(R.id.reports_header_name_sort_icon);
	// dateSortIcon.setVisibility(View.INVISIBLE);
	// scoreSortIcon.setVisibility(View.INVISIBLE);
	// manageSort(layout.getTag().toString(), nameSortIcon);
	// nameSortIcon.setVisibility(View.VISIBLE);
	// } else if (((String) layout.getTag()).matches("date")) {
	// dateSortIcon = (ImageView) tableLayout.findViewById(R.id.reports_header_date_sort_icon);
	// nameSortIcon.setVisibility(View.INVISIBLE);
	// scoreSortIcon.setVisibility(View.INVISIBLE);
	// manageSort(layout.getTag().toString(), dateSortIcon);
	// dateSortIcon.setVisibility(View.VISIBLE);
	// } else if (((String) layout.getTag()).matches("score")) {
	// scoreSortIcon = (ImageView) tableLayout.findViewById(R.id.reports_header_score_sort_icon);
	// nameSortIcon.setVisibility(View.INVISIBLE);
	// dateSortIcon.setVisibility(View.INVISIBLE);
	// manageSort(layout.getTag().toString(), scoreSortIcon);
	// scoreSortIcon.setVisibility(View.VISIBLE);
	// }
	// reload(false);
	// selectionMemory.setSelection(null,
	// gridContext.getResources().getDrawable(R.drawable.table_row_bg_unselected));
	// }
	// });
	// }
	//
	// private void manageSort(String tag, ImageView icon) {
	// orderBy = order.getOrder(tag);
	// if (orderBy.contains("asc")) {
	// icon.setImageResource(R.drawable.sort_up);
	// } else if (orderBy.contains("desc")) {
	// icon.setImageResource(R.drawable.sort_down);
	// }
	// }
	//
	// public void reload(boolean all) {
	// if (all) {
	// tableLayout.removeAllViews();
	// headerCreation();
	// } else {
	// tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
	// }
	// selectionMemory.setSelection(null, gridContext.getResources().getDrawable(R.drawable.table_row_bg_unselected));
	// executeTable();
	// }
	//
	// public void setQuery() {
	// this.columns = gridContext.getResources().getStringArray(R.array.columnAttributes);
	// this.selection = null;
	// this.selectionArgs = null;
	// this.groupBy = null;
	// this.having = null;
	// order = new OrderDirection(gridContext.getResources().getStringArray(R.array.orderedColumns));
	// this.orderBy = gridContext.getString(R.string.db_id_desc);
	// }
	//
	// public void executeTable() {
	// selectionMemory.setColorSelection(gridContext.getResources().getDrawable(R.drawable.table_row_bg_selected));
	// Cursor cursor = dataHandler.getDatabaseManager().query(
	// gridContext.getString(R.string.db_table_summarydata_name), columns, selection, selectionArgs, groupBy,
	// having, orderBy);
	// int[] ids = new int[cursor.getCount()];
	// int rowCount = 1;
	// String text = null;
	// if (cursor.moveToFirst()) {
	// do {
	// ViewGroup rom = (ViewGroup) LayoutInflater.from(gridContext).inflate(R.layout.reports_row_layout, null);
	// rom.setId(rowCount);
	// rom.setOnClickListener(new View.OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// selectionMemory.setSelection((ViewGroup) v,
	// gridContext.getResources().getDrawable(R.drawable.table_row_bg_unselected));
	//
	// Intent intent = new Intent(gridContext, SingleReportActivity.class);
	// intent.putExtra(gridContext.getClass().getName(), getSelection().getIds()[v.getId() - 1]);
	// // Create the view using QuestionaryActivityGroup's LocalActivityManager
	// View view = ReportActivityGroup.instance
	// .getLocalActivityManager()
	// .startActivity(SingleReportActivity.class.getSimpleName(),
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
	// // Again, replace the view
	// ReportActivityGroup.instance.replaceView(view);
	//
	// }
	// });
	// for (int columnCount = 0; columnCount < cursor.getColumnCount(); columnCount++) {
	// if (cursor.getColumnName(columnCount).matches("id")) {
	// ids[rowCount - 1] = cursor.getInt(columnCount);
	// } else {
	// if (isParsableToDate(cursor.getColumnName(columnCount))) {
	// long date_long = Long.parseLong(cursor.getString(columnCount));
	// Date date = new Date(date_long);
	// DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
	// text = df.format(date);
	// TextView cell = (TextView) rom.findViewById(R.id.reports_row_date);
	// cell.setText(text);
	// } else if (cursor.getColumnName(columnCount).matches("score")) {
	// float score = Float.parseFloat(cursor.getString(columnCount));
	// text = String.valueOf(score / 2);
	// TextView cell = (TextView) rom.findViewById(R.id.reports_row_score);
	// cell.setText(text);
	// } else {
	// text = cursor.getString(columnCount);
	// TextView cell = (TextView) rom.findViewById(R.id.reports_row_name);
	// cell.setSelected(true);
	// cell.setText(text);
	// }
	// }
	// }
	// tableLayout.addView(rom);
	// rowCount++;
	// } while (cursor.moveToNext());
	// }
	// selectionMemory.setIds(ids);
	// if (cursor != null && !cursor.isClosed()) {
	// cursor.close();
	// }
	// }

	private boolean isParsableToDate(String columnName) {
		if (columnName.matches("date")) {
			return true;
		}
		return false;
	}

	public String[] getAllValuesFromRow() {
		return (String[]) selectionMemory.getAllValuesFromRow();
	}

	public SelectionAndIdMemory getSelection() {
		return selectionMemory;
	}
}
