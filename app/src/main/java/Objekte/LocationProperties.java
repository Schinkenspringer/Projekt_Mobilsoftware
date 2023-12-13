package Objekte;

import com.google.gson.annotations.SerializedName;

public class LocationProperties {

    @SerializedName("distance")
    public double distance;

    public double getDistance() {
        return distance;
    }
}
