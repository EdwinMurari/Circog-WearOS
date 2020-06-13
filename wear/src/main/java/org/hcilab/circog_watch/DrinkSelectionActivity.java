package org.hcilab.circog_watch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DrinkSelectionActivity extends WearableActivity {

    /*
    Code to extract data from : https://www.caffeineinformer.com/the-caffeine-database
        var items = [];
        $('#caffeinedb').find('tr').each(function(i, e)
        {
            items.push({
                "drink" :  $(e.children[0]).text(),
                "fl_oz" : $(e.children[1]).text(),
                "caffeine_mg": $(e.children[2]).text(),
                "mg_per_floz": $(e.children[3]).text()
            });
        });
    */

    public final static String RESULT_SELECTED_DRINK = "resultSelectedDrink";

    private ArrayList<DrinkItem> drinkItems;
    private DrinkListAdapter drinkListAdapter;
    WearableRecyclerView wearableRecyclerView;
    private Button _btnConfirm;

    private static int _selectedDrinkIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_selection);

        wearableRecyclerView = findViewById(R.id.recycler_coffee_view);
        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);
        wearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
//        wearableRecyclerView.setCircularScrollingGestureEnabled(true);

        CustomScrollingLayoutCallback customScrollingLayoutCallback = new CustomScrollingLayoutCallback();
        wearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this, customScrollingLayoutCallback));

        drinkItems = new ArrayList<>();
        drinkListAdapter = new DrinkListAdapter(this, drinkItems, new DrinkListAdapter.AdapterCallback() {

            @Override
            public void onItemChecked(Integer itemIndex, boolean isChecked) {
                onDrinkSelected(itemIndex);
            }
        });
        wearableRecyclerView.setAdapter(drinkListAdapter);

        CaffeineDataTask caffeineDataTask = new CaffeineDataTask(this);
        caffeineDataTask.setOnTaskCompleteListener(onCaffeineDataReceived);
        caffeineDataTask.executeOnThreadPool();

        _btnConfirm = findViewById(R.id.btnConfirm);
        _btnConfirm.setVisibility(_selectedDrinkIndex == -1 ? View.GONE : View.VISIBLE);

        initSearchView();
    }

    private static final class CaffeineDataTask extends CallbackTask<List<DrinkItem>> {
        private Context _context;

        CaffeineDataTask(Context context) {
            _context = context;
        }

        @Override
        protected List<DrinkItem> doInBackground() throws Exception {
            return CoffeeDataManager.getInstance().getCoffeeData(_context);
        }
    }

    private CallbackTask.OnPostTaskComplete<List<DrinkItem>> onCaffeineDataReceived = new CallbackTask.OnPostTaskComplete<List<DrinkItem>>() {
        @Override
        public void onProcessFinished(List<DrinkItem> items) {
            drinkItems.addAll(items);
            drinkItems.add(0, new DrinkItem("Did not have any"));
            drinkListAdapter.notifyDataSetChanged();
        }
    };

    private void onDrinkSelected(int itemIndex) {
        _selectedDrinkIndex = itemIndex;

        if (_btnConfirm.getVisibility() == View.GONE)
            _btnConfirm.setVisibility(View.VISIBLE);
    }

    private void initSearchView() {
        SearchView searchView = findViewById(R.id.searchView);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                drinkListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                drinkListAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    public void onButtonConfirmClick(View view) {
        Intent intent = getIntent();
        intent.putExtra(RESULT_SELECTED_DRINK, _selectedDrinkIndex);
        setResult(RESULT_OK, intent);
        finish();
    }
}
