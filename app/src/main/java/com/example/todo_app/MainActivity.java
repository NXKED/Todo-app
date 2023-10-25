package com.example.todo_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import com.example.todo_app.MyAdapter.TodoItem;




public class MainActivity extends AppCompatActivity {

    private ArrayList<MyAdapter.TodoItem> items;
    private RecyclerView recyclerView;
    private Button button;
    private MyAdapter itemsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.recyclerView);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    additem(view);
            }
        });

        items = new ArrayList<>();
        itemsAdapter = new MyAdapter(this, items);
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemsAdapter);

        // adding a divider between the todos

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem(view);
            }
        });

        enableSwipeToDelete();
    }
    private boolean remove(int position) {
        Context context = getApplicationContext();
        Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show();
        items.remove(position);
        itemsAdapter.notifyDataSetChanged();
        return true;

    }

    private void additem(View view) {
        EditText input = findViewById(R.id.edit_text);
        String itemText = input.getText().toString();

        if(!itemText.isEmpty()) {
            TodoItem todoItem = new MyAdapter.TodoItem(itemText, false, true);
            items.add(0,todoItem);
            itemsAdapter.notifyDataSetChanged();
            input.setText("");
        }
        else {
            Toast.makeText(getApplicationContext(), "No Text entered", Toast.LENGTH_LONG).show();
        }
    }




    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(
                    @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                removeItem(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeItem(int position) {
        items.remove(position);
        itemsAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "Item removed", Toast.LENGTH_SHORT).show();
    }





}