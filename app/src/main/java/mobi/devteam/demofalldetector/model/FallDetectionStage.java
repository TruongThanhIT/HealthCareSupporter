package mobi.devteam.demofalldetector.model;

/**
 * Created by Administrator on 7/4/2017.
 */

public class FallDetectionStage {
    private double thresh_1;
    private double thresh_2;
    private double thresh_3;
    private double time;

    private boolean confirm_ok;
    private boolean recovery;

    public FallDetectionStage() {
    }

    public double getThresh_1() {
        return thresh_1;
    }

    public void setThresh_1(double thresh_1) {
        this.thresh_1 = thresh_1;
    }

    public double getThresh_2() {
        return thresh_2;
    }

    public void setThresh_2(double thresh_2) {
        this.thresh_2 = thresh_2;
    }

    public double getThresh_3() {
        return thresh_3;
    }

    public void setThresh_3(double thresh_3) {
        this.thresh_3 = thresh_3;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean isConfirm_ok() {
        return confirm_ok;
    }

    public void setConfirm_ok(boolean confirm_ok) {
        this.confirm_ok = confirm_ok;
    }

    public boolean isRecovery() {
        return recovery;
    }

    public void setRecovery(boolean recovery) {
        this.recovery = recovery;
    }
}
