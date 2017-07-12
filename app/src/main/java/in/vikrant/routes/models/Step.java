
package in.vikrant.routes.models;


public class Step {

    public Distance distance;
    public Duration duration;
    public LatLong end_location;
    public String html_instructions;
    public Polyline polyline;
    public LatLong start_location;
    public String travel_mode;
    public String maneuver;

    public Step(Distance distance, Duration duration, LatLong end_location, String html_instructions, Polyline polyline, LatLong start_location, String travel_mode, String maneuver) {
        this.distance = distance;
        this.duration = duration;
        this.end_location = end_location;
        this.html_instructions = html_instructions;
        this.polyline = polyline;
        this.start_location = start_location;
        this.travel_mode = travel_mode;
        this.maneuver = maneuver;
    }


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

    public LatLong getEnd_location() {
        return end_location;
    }

    public void setEnd_location(LatLong end_location) {
        this.end_location = end_location;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }

    public void setHtml_instructions(String html_instructions) {
        this.html_instructions = html_instructions;
    }

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public LatLong getStart_location() {
        return start_location;
    }

    public void setStart_location(LatLong start_location) {
        this.start_location = start_location;
    }

    public String getTravel_mode() {
        return travel_mode;
    }

    public void setTravel_mode(String travel_mode) {
        this.travel_mode = travel_mode;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }
}
