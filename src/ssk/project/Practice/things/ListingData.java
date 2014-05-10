package ssk.project.Practice.things;

public class ListingData {

	private ThingListing[] children;
	private String after;
	private String before;
	private String modhash;
	
	public ThingListing[] getChildren() {
		return children;
	}
	public void setChildren(ThingListing[] children) {
		this.children = children;
	}
	public String getAfter() {
		return after;
	}
	public void setAfter(String after) {
		this.after = after;
	}
	public String getBefore() {
		return before;
	}
	public void setBefore(String before) {
		this.before = before;
	}
	public String getModhash() {
		return modhash;
	}
	public void setModhash(String modhash) {
		this.modhash = modhash;
	}
	
}
