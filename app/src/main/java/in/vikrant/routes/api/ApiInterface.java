package in.vikrant.routes.api;


import in.vikrant.routes.models.GetDirection;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ApiInterface {

    // https://maps.googleapis.com/maps/api/directions/json?origin=12.9147914,77.6461049&destination=12.9159535,77.6439628&key=AIzaSyC8ROFPB3ONCBSXppy8TniUkuen5Y0i2Vk
    @GET("directions/json")
    Call<GetDirection> getDirection(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("alternative") String alternative,
            @Query("key") String key
    );

}