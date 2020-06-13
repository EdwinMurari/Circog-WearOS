package org.hcilab.circog_watch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class DrinkListAdapter extends RecyclerView.Adapter<DrinkListAdapter.RecyclerViewHolder> implements Filterable {
    private ArrayList<DrinkItem> _drinkList;
    private ArrayList<DrinkItem> _drinkListFiltered;

    public interface AdapterCallback {
        void onItemChecked(Integer itemIndex, boolean isChecked);
    }

    private AdapterCallback callback;
    private Context context;
    public static int radioCheckedIndex = 0;


    public DrinkListAdapter(Context context, ArrayList<DrinkItem> dataArgs, AdapterCallback callback) {
        this.context = context;
        this._drinkList = dataArgs;
        this.callback = callback;
        this._drinkListFiltered = _drinkList;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coffee_recyclerview_item, parent, false);

        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);

        return recyclerViewHolder;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemContainer;
        RadioButton radioButton;

        public RecyclerViewHolder(View view) {
            super(view);
            itemContainer = view.findViewById(R.id.coffee_item_container);
            radioButton = view.findViewById(R.id.coffeRadioButton);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerViewHolder holder, final int position) {
        DrinkItem drinkItem = _drinkListFiltered.get(position);

        holder.radioButton.setText(drinkItem.getName());
        holder.radioButton.setChecked(position == radioCheckedIndex);
        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.radioButton.isChecked()) {
                    int previousIndex = radioCheckedIndex;

                    holder.radioButton.setChecked(true);
                    radioCheckedIndex = position;

                    if (callback != null)
                        callback.onItemChecked(position, holder.radioButton.isChecked());

                    notifyItemChanged(previousIndex);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return _drinkListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchString = charSequence.toString();
                if (searchString.isEmpty()) {
                    _drinkListFiltered = _drinkList;
                } else {
                    ArrayList<DrinkItem> filteredList = new ArrayList<>();
                    for (DrinkItem drinkItem : _drinkList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (drinkItem.getName().toLowerCase().contains(searchString.toLowerCase())) {
                            filteredList.add(drinkItem);
                        }
                    }

                    _drinkListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = _drinkListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                _drinkListFiltered = (ArrayList<DrinkItem>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }
}
