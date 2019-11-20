package com.example.dictophone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaHolder> {

    private List<String> fileList;
    private TextView mTv;


    public void setItems(List<String> fileNames, TextView tv) {
        mTv = tv;
        fileList = fileNames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MediaHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaHolder holder, int position) {
        final String files = fileList.get(position);
        holder.fileName.setText(files);
        holder.fileName.setOnClickListener(v -> {
            if (mTv != null) {
                mTv.setText(fileList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }



    static class MediaHolder extends RecyclerView.ViewHolder {

        private final TextView fileName;


        public MediaHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name_tv);
        }
    }
}
