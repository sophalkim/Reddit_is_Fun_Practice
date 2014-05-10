package ssk.project.Practice.things;

public class Listing {

	private String kind;
	private ListingData data;
	
	public Listing() {}
	
	public Listing(String stuff) {
		kind = null;
		data = null;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public ListingData getData() {
		return data;
	}

	public void setData(ListingData data) {
		this.data = data;
	}
	
	
}
