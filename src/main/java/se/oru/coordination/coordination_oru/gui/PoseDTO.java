package se.oru.coordination.coordination_oru.gui;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PoseDTO {
    @JsonProperty("x")
    private double x;

    @JsonProperty("y")
    private double y;

    @JsonProperty("angle")
    private double angle;

    public PoseDTO() {
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
