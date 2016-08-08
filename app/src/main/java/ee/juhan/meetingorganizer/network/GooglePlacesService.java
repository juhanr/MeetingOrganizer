package ee.juhan.meetingorganizer.network;

import ee.juhan.meetingorganizer.models.googleplaces.PlaceSearchResult;
import retrofit.http.GET;
import retrofit.http.Query;

public interface GooglePlacesService {

	@GET("/nearbysearch/json")
	PlaceSearchResult getNearbyPlaces(@Query("location") String location,
			@Query("radius") int radius, @Query("type") String type);
}
