package com.example.bottomnavi.frag1_place;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class Frag1ViewModel extends ViewModel {
    private MutableLiveData<List<YourItem>> items;

    public LiveData<List<YourItem>> getItems() {
        if (items == null) {
            items = new MutableLiveData<>();
        }
        return items;
    }

    public void setItems(List<YourItem> itemList) {
        if (items == null) {
            items = new MutableLiveData<>();
        }
        items.setValue(itemList);
    }
}