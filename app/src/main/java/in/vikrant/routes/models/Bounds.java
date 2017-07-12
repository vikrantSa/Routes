
package in.vikrant.routes.models;


public class Bounds {

    public LatLong northeast;
    public LatLong southwest;

    public LatLong getNortheast() {
        return northeast;
    }

    public void setNortheast(LatLong northeast) {
        this.northeast = northeast;
    }

    public LatLong getSouthwest() {
        return southwest;
    }

    public void setSouthwest(LatLong southwest) {
        this.southwest = southwest;
    }
}
