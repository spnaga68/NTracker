package pasu.ntracker;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by developer on 26/3/18.
 */

public interface ApiInterface {
    String BASE_URL = "https://maps.googleapis.com";

    @GET("/maps/api/geocode/json")
    Call<ResponseBody> getCityResults(@Query("latlng") String location, @Query("sensor") boolean radius);
}
