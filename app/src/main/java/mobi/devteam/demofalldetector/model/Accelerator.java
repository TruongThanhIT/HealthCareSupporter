package mobi.devteam.demofalldetector.model;

/**
 * Created by Administrator on 6/29/2017.
 */

public class Accelerator {
    private double x;
    private double y;
    private double z;

    /**
     * Init by value return by event
     *
     * @param linear_acceleration
     */
    public Accelerator(double[] linear_acceleration) {
        this.x = linear_acceleration[0];
        this.y = linear_acceleration[0];
        this.z = linear_acceleration[0];
    }

    /**
     * These x,y,z stand for x,y,z accelerator
     *
     * @param x
     * @param y
     * @param z
     */
    public Accelerator(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
