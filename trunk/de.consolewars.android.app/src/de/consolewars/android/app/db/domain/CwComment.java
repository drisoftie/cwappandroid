package de.consolewars.android.app.db.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "Comment")
@Root(name = "item", strict = false)
public class CwComment extends CwEntity {

	@DatabaseField(columnName = "cid")
	@Element(name = "cid", required = false)
	private int cid;
	@DatabaseField(columnName = "commentcount")
	@Element(name = "commentcount", required = false)
	private int commentcount;
	@DatabaseField(columnName = "currpage")
	@Element(name = "currpage", required = false)
	private int currpage;
	@DatabaseField(columnName = "mode")
	@Element(name = "mode", required = false)
	private String mode;
	@DatabaseField(columnName = "pagecount")
	@Element(name = "pagecount", required = false)
	private int pagecount;
	@DatabaseField(columnName = "postcount")
	@Element(name = "postcount", required = false)
	private int postcount;
	@DatabaseField(columnName = "quote")
	@Element(name = "quote", required = false)
	private String quote;
	@DatabaseField(columnName = "statement")
	@Element(name = "statement", required = false)
	private String statement;
	@DatabaseField(columnName = "uid")
	@Element(name = "uid", required = false)
	private int uid;
	@DatabaseField(columnName = "unixtime")
	@Element(name = "unixtime", required = false)
	private int unixtime;
	@DatabaseField(columnName = "username")
	@Element(name = "username", required = false)
	private String username;
	@DatabaseField(columnName = "usertitle")
	@Element(name = "usertitle", required = false)
	private String usertitle;

	@DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "news_id")
	@Transient
	private CwNews cwNews;

	/**
	 * Mandatory
	 */
	public CwComment() {
	}

	/**
	 * @param cid
	 * @param commentcount
	 * @param currpage
	 * @param mode
	 * @param pagecount
	 * @param postcount
	 * @param quote
	 * @param statement
	 * @param uid
	 * @param unixtime
	 * @param username
	 * @param usertitle
	 * @param cwNews
	 */
	public CwComment(int cid, int commentcount, int currpage, String mode, int pagecount, int postcount, String quote,
			String statement, int uid, int unixtime, String username, String usertitle, CwNews cwNews) {
		super();
		this.cid = cid;
		this.commentcount = commentcount;
		this.currpage = currpage;
		this.mode = mode;
		this.pagecount = pagecount;
		this.postcount = postcount;
		this.quote = quote;
		this.statement = statement;
		this.uid = uid;
		this.unixtime = unixtime;
		this.username = username;
		this.usertitle = usertitle;
		this.cwNews = cwNews;
	}

	/**
	 * @return the cid
	 */
	public int getCid() {
		return cid;
	}

	/**
	 * @param cid
	 *            the cid to set
	 */
	public void setCid(int cid) {
		this.cid = cid;
	}

	/**
	 * @return the commentcount
	 */
	public int getCommentcount() {
		return commentcount;
	}

	/**
	 * @param commentcount
	 *            the commentcount to set
	 */
	public void setCommentcount(int commentcount) {
		this.commentcount = commentcount;
	}

	/**
	 * @return the currpage
	 */
	public int getCurrpage() {
		return currpage;
	}

	/**
	 * @param currpage
	 *            the currpage to set
	 */
	public void setCurrpage(int currpage) {
		this.currpage = currpage;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return the pagecount
	 */
	public int getPagecount() {
		return pagecount;
	}

	/**
	 * @param pagecount
	 *            the pagecount to set
	 */
	public void setPagecount(int pagecount) {
		this.pagecount = pagecount;
	}

	/**
	 * @return the postcount
	 */
	public int getPostcount() {
		return postcount;
	}

	/**
	 * @param postcount
	 *            the postcount to set
	 */
	public void setPostcount(int postcount) {
		this.postcount = postcount;
	}

	/**
	 * @return the quote
	 */
	public String getQuote() {
		return quote;
	}

	/**
	 * @param quote
	 *            the quote to set
	 */
	public void setQuote(String quote) {
		this.quote = quote;
	}

	/**
	 * @return the statement
	 */
	public String getStatement() {
		return statement;
	}

	/**
	 * @param statement
	 *            the statement to set
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/**
	 * @return the uid
	 */
	public int getUid() {
		return uid;
	}

	/**
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(int uid) {
		this.uid = uid;
	}

	/**
	 * @return the unixtime
	 */
	public int getUnixtime() {
		return unixtime;
	}

	/**
	 * @param unixtime
	 *            the unixtime to set
	 */
	public void setUnixtime(int unixtime) {
		this.unixtime = unixtime;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the usertitle
	 */
	public String getUsertitle() {
		return usertitle;
	}

	/**
	 * @param usertitle
	 *            the usertitle to set
	 */
	public void setUsertitle(String usertitle) {
		this.usertitle = usertitle;
	}

	/**
	 * @return the cwNews
	 */
	public CwNews getCwNews() {
		return cwNews;
	}

	/**
	 * @param cwNews
	 *            the cwNews to set
	 */
	public void setCwNews(CwNews cwNews) {
		this.cwNews = cwNews;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(cid).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!this.getClass().isInstance(obj)) {
			return false;
		}
		CwComment other = (CwComment) obj;
		return new EqualsBuilder().append(cid, other.getCid()).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(getCid()).toString();
	}
}
