package se.oru.coordination.coordination_oru.gui;

import java.util.List;

public class MapData {
    private String image;
    private double resolution;
    private List<Double> origin;
    private int negate;
    private double occupied_thresh;
//    public String getImageYAML() {
//        return image.replaceAll("map\\..*", "map.yaml");
//    } // FIXME Adhoc solution

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public List<Double> getOrigin() {
        return origin;
    }

    public void setOrigin(List<Double> origin) {
        this.origin = origin;
    }

    public int getNegate() {
        return negate;
    }

    public void setNegate(int negate) {
        this.negate = negate;
    }

    public double getOccupied_thresh() {
        return occupied_thresh;
    }

    public void setOccupied_thresh(double occupied_thresh) {
        this.occupied_thresh = occupied_thresh;
    }
}
