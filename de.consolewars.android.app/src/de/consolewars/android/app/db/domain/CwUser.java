package de.consolewars.android.app.db.domain;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Userdata")
public class CwUser extends CwEntity {
	@DatabaseField(columnName = "name")
	private String name;
	@DatabaseField(columnName = "hashPassword")
	private String hashPassword;
	@DatabaseField(columnName = "date", dataType = DataType.DATE)
	private Date date;
	@DatabaseField(columnName = "lastNewsId")
	private int lastNewsId;
	@DatabaseField(columnName = "lastBlogId")
	private int lastBlogId;

	public CwUser() {
	}

	public CwUser(String name, String hashPassword, Date date, int lastNewsId, int lastBlogId) {
		this.name = name;
		this.hashPassword = hashPassword;
		this.date = date;
		this.lastNewsId = lastNewsId;
		this.lastBlogId = lastBlogId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(String hashPassword) {
		this.hashPassword = hashPassword;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getLastNewsId() {
		return lastNewsId;
	}

	public void setLastNewsId(int lastNewsId) {
		this.lastNewsId = lastNewsId;
	}

	public int getLastBlogId() {
		return lastBlogId;
	}

	public void setLastBlogId(int lastBlogId) {
		this.lastBlogId = lastBlogId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getId()).append(name).toString();
	}
}
