package com.example.todo_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompletedItemsAdapter extends RecyclerView.Adapter<CompletedItemsAdapter.ViewHolder> {
    private List<MyAdapter.TodoItem> completedItems;
    private Context context;

    public CompletedItemsAdapter(Context context, List<MyAdapter.TodoItem> completedItems) {
        this.completedItems = completedItems;
        this.context = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_done, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyAdapter.TodoItem item = completedItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return completedItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView taskTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTextView = itemView.findViewById(R.id.taskName);
        }
        public void bind(MyAdapter.TodoItem item) {
            taskTextView.setText(item.getTaskName());
        }
    }
}
