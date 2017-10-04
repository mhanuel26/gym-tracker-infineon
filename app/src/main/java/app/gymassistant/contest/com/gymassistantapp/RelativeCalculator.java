package app.gymassistant.contest.com.gymassistantapp;

import java.util.ArrayList;


/**
 * Created by mhanuel on 9/29/17.
 */

public class RelativeCalculator {
    public static final int DEFAULT_MEAN_VALUE_COUNT = 10;
    private static final int MAX_SAMPLES = 10;
    private final String TAG = RelativeCalculator.class.getName();
    private double curValue;
    private double filtValue;
    private double meanValue;
    private int meanValueCount;
    private double relativeValue;
    private int valueCount;
    private double valueSum;
    private ArrayList<Double> samples;

    public RelativeCalculator() {
        this.reset();
        this.samples = new ArrayList<Double>();
        this.meanValueCount = 10;
    }

    public RelativeCalculator(int var1) {
        this.reset();
        this.samples = new ArrayList<Double>();
        this.meanValueCount = var1;
    }

    public double getRelativeValue() {
        return this.relativeValue;
    }

    public boolean isMeanBuilt() {
        return this.meanValueCount == this.valueCount;
    }

    public void reset() {
        this.valueCount = 0;
        this.meanValue = 0.0D;
        this.valueSum = 0.0D;
        this.relativeValue = 0.0D;
        this.curValue = 0.0D;
    }

    private double mean(ArrayList<Double> nums){
        double res = 0, sum = 0;
        for(Double d : nums)
            sum += d;
        res = sum / (double)(Math.max(nums.size(), 1));
        if(nums.size() >= MAX_SAMPLES){
            nums.remove(0);
        }
        return res;
    }

    public void updateValue(double var1) {
        this.curValue = var1;

        if(this.valueCount < this.meanValueCount) {
            ++this.valueCount;
            this.valueSum += var1;
            this.meanValue = this.valueSum / (double)this.valueCount;
        }

        this.relativeValue = this.curValue - this.meanValue;
    }

//    public void updateValue(double var1) {
//        this.curValue = var1;
//        this.samples.add(var1);
//        this.filtValue = this.mean(samples);
//
//        if(this.valueCount < this.meanValueCount) {
//            ++this.valueCount;
//            this.valueSum += this.filtValue;
//            this.meanValue = this.valueSum / (double)this.valueCount;
//        }
//
//        this.relativeValue = this.filtValue - this.meanValue;
//    }
}