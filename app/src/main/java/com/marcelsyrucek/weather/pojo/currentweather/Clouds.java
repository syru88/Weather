
package com.marcelsyrucek.weather.pojo.currentweather;

import com.google.gson.annotations.Expose;

public class Clouds {

    @Expose
    private double all;

    /**
     * 
     * @return
     *     The all
     */
    public double getAll() {
        return all;
    }

    /**
     * 
     * @param all
     *     The all
     */
    public void setAll(int all) {
        this.all = all;
    }

}
