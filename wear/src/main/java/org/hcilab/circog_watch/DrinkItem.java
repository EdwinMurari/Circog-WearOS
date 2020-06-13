package org.hcilab.circog_watch;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

class DrinkItem {
    private String drink;
    private double fluidOunce;
    private int caffeineContentInMilliGrams;
    private double mgPerFlOz;

    DrinkItem(String name) {
        this.drink = name;
    }

    DrinkItem(String name, double fluidOunce, int caffeineContentInMilliGrams, double mgPerFlOz) {
        this.drink = name;
        this.fluidOunce = fluidOunce;
        this.caffeineContentInMilliGrams = caffeineContentInMilliGrams;
        this.mgPerFlOz = mgPerFlOz;
    }

    public String getName() {
        return drink;
    }

    public double getFluidOunce() {
        return fluidOunce;
    }

    public int getCaffeineContentInMilliGrams() {
        return caffeineContentInMilliGrams;
    }

    public double getMgPerFlOz() {
        return mgPerFlOz;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("drink", getName());
            jsonObject.put("fl_oz", getFluidOunce());
            jsonObject.put("caffeine_mg", getCaffeineContentInMilliGrams());
            jsonObject.put("mg_per_floz", getMgPerFlOz());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
