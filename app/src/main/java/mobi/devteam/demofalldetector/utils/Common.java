package mobi.devteam.demofalldetector.utils;

/**
 * Created by Administrator on 7/4/2017.
 */

public class Common {
    /**
     * These config bellow is default for fall detection
     * Reference to : https://docs.google.com/document/d/10oyGD1t4b-33Hpn5z-S2A6N89S5ptq18Yzw1KPywlE0
     */
    public static final double DEFAULT_THRESHOLD_1 = 6f;
    public static final double DEFAULT_THRESHOLD_2 = 5f;
    public static final double DEFAULT_THRESHOLD_3 = 70f;

    //Threshold 1
    public static final float T1_AGE_LT_60 = 0.5f;
    public static final float T1_AGE_GT_60 = -0.5f;

    public static final float T1_IS_MALE = 0.5f;
    public static final float T1_IS_FEMALE = -0.5f;

    public static final float T1_BMI_18 = -0.5f;
    public static final float T1_BMI_25_30 = 0.5f;
    public static final float T1_BMI_30 = 1f;

    //Threshold 2
    public static final float T2_AGE_LT_60 = 0.3f;
    public static final float T2_AGE_GT_60 = -0.3f;

    public static final float T2_IS_MALE = 0.3f;
    public static final float T2_IS_FEMALE = -0.3f;

    public static final float T2_BMI_18 = -0.5f;
    public static final float T2_BMI_25_30 = 0.5f;
    public static final float T2_BMI_30 = 1f;

    //Threshold 3
    public static final float T3_AGE_LT_60 = 0.3f;
    public static final float T3_AGE_GT_60 = -0.3f;

    public static final float T3_IS_MALE = 0.3f;
    public static final float T3_IS_FEMALE = -0.3f;

    public static final float T3_BMI_18 = -0.5f;
    public static final float T3_BMI_25_30 = 0.5f;
    public static final float T3_BMI_30 = 1f;

    public static final long WAITING_FOR_CONFIRM = 30000;
}
