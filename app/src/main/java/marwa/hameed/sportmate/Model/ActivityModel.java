package marwa.hameed.sportmate.Model;

public class ActivityModel
{
    private String distance,name,imageurl,type,time,date;
    private double latitude_from,longitude_from,latitude_to,longitude_to;

    public ActivityModel() {
    }

    public ActivityModel(String distance, String name, String imageurl, String type, String time, String date, double latitude_from, double longitude_from, double latitude_to, double longitude_to) {
        this.distance = distance;
        this.name = name;
        this.imageurl = imageurl;
        this.type = type;
        this.time = time;
        this.date = date;
        this.latitude_from = latitude_from;
        this.longitude_from = longitude_from;
        this.latitude_to = latitude_to;
        this.longitude_to = longitude_to;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude_from() {
        return latitude_from;
    }

    public void setLatitude_from(double latitude_from) {
        this.latitude_from = latitude_from;
    }

    public double getLongitude_from() {
        return longitude_from;
    }

    public void setLongitude_from(double longitude_from) {
        this.longitude_from = longitude_from;
    }

    public double getLatitude_to() {
        return latitude_to;
    }

    public void setLatitude_to(double latitude_to) {
        this.latitude_to = latitude_to;
    }

    public double getLongitude_to() {
        return longitude_to;
    }

    public void setLongitude_to(double longitude_to) {
        this.longitude_to = longitude_to;
    }
}
