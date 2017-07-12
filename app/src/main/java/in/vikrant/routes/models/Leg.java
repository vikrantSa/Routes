
package in.vikrant.routes.models;

import java.util.ArrayList;
import java.util.List;

public class Leg {

    public Distance distance;
    public Duration duration;
    public String end_address;
    public LatLong end_location;
    public String start_address;
    public LatLong start_location;
    public List<Step> steps = new ArrayList<>();

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getEnd_address() {
        return end_address;
    }

    public void setEnd_address(String end_address) {
        this.end_address = end_address;
    }

    public LatLong getEnd_location() {
        return end_location;
    }

    public void setEnd_location(LatLong end_location) {
        this.end_location = end_location;
    }

    public String getStart_address() {
        return start_address;
    }

    public void setStart_address(String start_address) {
        this.start_address = start_address;
    }

    public LatLong getStart_location() {
        return start_location;
    }

    public void setStart_location(LatLong start_location) {
        this.start_location = start_location;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
