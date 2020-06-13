package org.hcilab.circog_watch;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class CoffeeDataManager {

    private static CoffeeDataManager _instance;

    private List<DrinkItem> drinkItemList;

    private CoffeeDataManager() {

    }

    static CoffeeDataManager getInstance() {
        if (_instance == null)
            _instance = new CoffeeDataManager();

        return _instance;
    }

    List<DrinkItem> getCoffeeData(Context context) throws JSONException {
        if (drinkItemList == null) {
            drinkItemList = new ArrayList<>();

            String caffeineDataJson = JSONUtil.loadJSONFromAsset(context, "caffeine_data.json");
            if (caffeineDataJson != null) {
                JSONArray caffeineDataArray = new JSONArray(caffeineDataJson);
                for (int index = 0; index < caffeineDataArray.length(); index++) {
                    JSONObject drinkObject = caffeineDataArray.getJSONObject(index);
                    String name = drinkObject.getString("drink");
                    double fluidOunce = drinkObject.getDouble("fl_oz");
                    int caffeineContentInMilliGrams = drinkObject.getInt("caffeine_mg");
                    double mgPerFlOz = drinkObject.getDouble("mg_per_floz");

                    drinkItemList.add(new DrinkItem(name, fluidOunce, caffeineContentInMilliGrams, mgPerFlOz));
                }
            }
        }

        return drinkItemList;
    }

    DrinkItem getCoffeeData(int index){
        if(drinkItemList == null)
            return null;
        return drinkItemList.get(index);
    }
}
