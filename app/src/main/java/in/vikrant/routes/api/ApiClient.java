package in.vikrant.routes.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {
    //https://maps.googleapis.com/maps/api/directions/json?origin=12.9147914,77.6461049&destination=12.9159535,77.6439628&key=AIzaSyC8ROFPB3ONCBSXppy8TniUkuen5Y0i2Vk
    private static final String BASE_URL1 = "https://maps.googleapis.com/maps/api/";
    private static Retrofit retrofit1 = null;

    public static Retrofit getClient() {
        if (retrofit1 == null) {
            retrofit1 = new Retrofit.Builder()
                    .baseUrl(BASE_URL1)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit1;
    }

}