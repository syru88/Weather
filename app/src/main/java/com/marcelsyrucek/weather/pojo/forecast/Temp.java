
package com.marcelsyrucek.weather.pojo.forecast;

import java.util.HashMap;
import java.util.Map;

public class Temp {

    private double day;
    private double min;
    private double max;
    private double night;
    private double eve;
    private double morn;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The day
     */
    public double getDay() {
        return day;
    }

    /**
     * 
     * @param day
     *     The day
     */
    public void setDay(double day) {
        this.day = day;
    }

    /**
     * 
     * @return
     *     The min
     */
    public double getMin() {
        return min;
    }

    /**
     * 
     * @param min
     *     The min
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * 
     * @return
     *     The max
     */
    public double getMax() {
        return max;
    }

    /**
     * 
     * @param max
     *     The max
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * 
     * @return
     *     The night
     */
    public double getNight() {
        return night;
    }

    /**
     * 
     * @param night
     *     The night
     */
    public void setNight(double night) {
        this.night = night;
    }

    /**
     * 
     * @return
     *     The eve
     */
    public double getEve() {
        return eve;
    }

    /**
     * 
     * @param eve
     *     The eve
     */
    public void setEve(double eve) {
        this.eve = eve;
    }

    /**
     * 
     * @return
     *     The morn
     */
    public double getMorn() {
        return morn;
    }

    /**
     * 
     * @param morn
     *     The morn
     */
    public void setMorn(double morn) {
        this.morn = morn;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
