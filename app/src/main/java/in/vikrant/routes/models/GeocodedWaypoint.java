
package in.vikrant.routes.models;

import java.util.ArrayList;
import java.util.List;

public class GeocodedWaypoint {

    public String geocoder_status;
    public String place_id;
    public List<String> types = new ArrayList<>();

    public String getGeocoder_status() {
        return geocoder_status;
    }

    public void setGeocoder_status(String geocoder_status) {
        this.geocoder_status = geocoder_status;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
