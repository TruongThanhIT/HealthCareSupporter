package mobi.devteam.demofalldetector.model;


public class FallDetectionStage {
    private double thresh_1;
    private double thresh_2;
    private double thresh_3;
    private long time;
    private Accelerator accelerator_log;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
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

    public Accelerator getAccelerator_log() {
        return accelerator_log;
    }

    public void setAccelerator_log(Accelerator accelerator_log) {
        this.accelerator_log = accelerator_log;
    }
}
