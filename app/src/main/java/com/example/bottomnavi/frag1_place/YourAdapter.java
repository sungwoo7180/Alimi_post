package com.example.bottomnavi.frag1_place;
// YourAdapter.java
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottomnavi.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class YourAdapter extends RecyclerView.Adapter<YourAdapter.ViewHolder> {

    private List<YourItem> items;

    public YourAdapter(List<YourItem> items) {
        this.items = items;
    }

    public void setItems(List<YourItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YourItem item = items.get(position);

        // 이미지 로딩 (Picasso 예제)
        Log.d("Picasso", "Loading image from URL: " + item.getImageUrl());

        // 이미지 URL이 null이 아닌 경우에만 Picasso를 통해 이미지 로딩
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(holder.imageView);
        } else {
            // 이미지 URL이 null이거나 비어있는 경우에는 기본 이미지 등의 처리를 할 수 있음
            // 예를 들어 기본 이미지를 설정하거나, 숨겨진 ImageView를 보여주거나 등의 처리
            // holder.imageView.setImageResource(R.drawable.default_image);
            // holder.imageView.setVisibility(View.GONE);
        }
        holder.textViewNickName.setText(item.getNickname());
        holder.textView.setText(item.getText());
        holder.textViewLocation.setText(item.getLocation());
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNickName;
        ImageView imageView;
        TextView textView;
        TextView textViewLocation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNickName = itemView.findViewById(R.id.itemTextViewNickName);
            imageView = itemView.findViewById(R.id.itemImageView);
            textView = itemView.findViewById(R.id.itemTextView);
            textViewLocation = itemView.findViewById(R.id.itemTextViewLocation);


        }
    }
}