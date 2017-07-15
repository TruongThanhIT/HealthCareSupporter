package mobi.devteam.demofalldetector.model;

public class Profile {
    private double height;
    private double weight;
    private int age;
    private boolean male;

    private boolean detect_fall;
    private boolean allow_find;

    private double sensitive;

    private double thresh1;
    private double thresh2;
    private double thresh3;

    public Profile() {
    }

    public Profile(double height, double weight, int age, boolean male, boolean detect_fall, boolean allow_find, double sensitive, double thresh1, double thresh2, double thresh3) {
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.male = male;
        this.detect_fall = detect_fall;
        this.allow_find = allow_find;
        this.sensitive = sensitive;
        this.thresh1 = thresh1;
        this.thresh2 = thresh2;
        this.thresh3 = thresh3;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isDetect_fall() {
        return detect_fall;
    }

    public void setDetect_fall(boolean detect_fall) {
        this.detect_fall = detect_fall;
    }

    public boolean isAllow_find() {
        return allow_find;
    }

    public void setAllow_find(boolean allow_find) {
        this.allow_find = allow_find;
    }

    public double getThresh1() {
        return thresh1;
    }

    public void setThresh1(double thresh1) {
        this.thresh1 = thresh1;
    }

    public double getThresh2() {
        return thresh2;
    }

    public void setThresh2(double thresh2) {
        this.thresh2 = thresh2;
    }

    public double getThresh3() {
        return thresh3;
    }

    public void setThresh3(double thresh3) {
        this.thresh3 = thresh3;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSensitive() {
        return sensitive;
    }

    public void setSensitive(double sensitive) {
        this.sensitive = sensitive;
    }
}
