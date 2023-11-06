package com.example.todo_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.widget.CheckBox;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Context context;
    private ArrayList<TodoItem> items;
    private OnItemLongClickListener longClickListener;


    public MyAdapter(Context context, ArrayList<TodoItem> items, OnItemLongClickListener longClickListener) {
        this.context = context;
        this.items = items;
        this.longClickListener = longClickListener;
    }

    public static class TodoItem {
        private String taskName;
        private boolean completed;

        private String taskCreator;
        private String key;
        private String taskTime;

        public TodoItem() {

        }

        public TodoItem(String taskName, String taskCreator, boolean completed) {
            this.taskName = taskName;
            this.completed = completed;
            this.taskCreator = taskCreator;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getTaskCreator() {
            return taskCreator;
        }
        public void setTaskCreator(String taskCreator) {
            this.taskCreator = taskCreator;
        }

        public void setTaskTime (String dueDate) {
            this.taskTime = dueDate;
        }

        public String getTaskTime() {
            return taskTime;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey(){
            return key;
        }
    }


    //method to set items for filter
    public void setItems(ArrayList<TodoItem> items) {
        this.items = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TodoItem item = items.get(position);
        holder.textView.setText(item.getTaskName());
        holder.creatorView.setText(item.getTaskCreator());
        holder.timeView.setText(item.getTaskTime());

        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
               MyAdapter.TodoItem item = items.get(position);
               item.setCompleted(isChecked);

               String itemKey = item.getKey();
               if(itemKey != null && !itemKey.isEmpty()) {
                   DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("items").child(itemKey);
                   databaseReference.child("completed").setValue(isChecked);
               } else {
                   Log.e("my adapter"," invalid key for item");
               }
           }
       });

        holder.checkbox.setChecked(item.isCompleted());

        // on long click listener
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onItemLongClick(position);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        TextView creatorView;
        CheckBox checkbox;
        TextView timeView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.taskName);
            checkbox = itemView.findViewById(R.id.checkboxCompleted);
            creatorView = itemView.findViewById(R.id.taskCreator);
            timeView = itemView.findViewById(R.id.taskTime);
        }
    }

    // implementing long Item click
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }




}


