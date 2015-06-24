
package com.marcelsyrucek.weather.pojo.forecast;

import com.marcelsyrucek.weather.database.model.CityModel;
import com.marcelsyrucek.weather.pojo.WebErrorData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ForecastPojo extends WebErrorData {

    private City city;
    private int cnt;
    private java.util.List<com.marcelsyrucek.weather.pojo.forecast.List> list = new ArrayList<com.marcelsyrucek.weather.pojo.forecast.List>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public CityModel getCityModel() {
        CityModel cityModel = new CityModel();

        if (city != null && city.getCoord() != null) {
            cityModel.setId(city.getId()+"");
            cityModel.setName(getCityWithCountry());
            cityModel.setLatitude(city.getCoord().getLat());
            cityModel.setLongitude(city.getCoord().getLon());
        }

        return cityModel;
    }

    public String getCityWithCountry() {
        if (city != null && city.getCountry() != null) {
            return city.getName() + ", " + city.getCountry();
        } else {
            return city.getName();
        }
    }

    /**
     * 
     * @return
     *     The city
     */
    public City getCity() {
        return city;
    }

    /**
     * 
     * @param city
     *     The city
     */
    public void setCity(City city) {
        this.city = city;
    }

    /**
     * 
     * @return
     *     The cnt
     */
    public int getCnt() {
        return cnt;
    }

    /**
     * 
     * @param cnt
     *     The cnt
     */
    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    /**
     * 
     * @return
     *     The list
     */
    public java.util.List<com.marcelsyrucek.weather.pojo.forecast.List> getList() {
        return list;
    }

    /**
     * 
     * @param list
     *     The list
     */
    public void setList(java.util.List<com.marcelsyrucek.weather.pojo.forecast.List> list) {
        this.list = list;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
