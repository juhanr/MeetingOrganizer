package ee.juhan.meetingorganizer.models.googleplaces;

import java.util.List;

public class PlaceSearchResult {

	List<Place> results;

	@Override
	public String toString() {
		return "PlaceSearchResult{" +
				"results=" + results +
				'}';
	}

	public List<Place> getResults() {
		return results;
	}

	public void setResults(List<Place> results) {
		this.results = results;
	}
}
