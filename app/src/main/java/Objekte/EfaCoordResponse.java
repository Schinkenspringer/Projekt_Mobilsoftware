package Objekte;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EfaCoordResponse {

    @SerializedName("version")
    public String version;

    @SerializedName("locations")
    public List<Location> locations;

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    public String getId(){
        return id;
    }

    public String getNames(){
        return name;
    }


    public String getVersions(){
        return version;
    }

    public List<Location> getLocations(){
        return (locations);
    }





}
