package com.example.todo_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import com.example.todo_app.MyAdapter.TodoItem;




public class MainActivity extends AppCompatActivity {

    private ArrayList<MyAdapter.TodoItem> items;
    private RecyclerView recyclerView;
    private Button button;
    private MyAdapter itemsAdapter;
    private TextView itemCountTextView;
    private int itemCounter = 0;
    boolean itsMe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        button = findViewById(R.id.button);
        itemCountTextView = findViewById(R.id.todoCount);
        CheckBox checkboxMe = findViewById(R.id.checkboxMe);



        itemCountTextView.setText(String.valueOf(itemCounter));
        button.setOnClickListener(view -> addItem());

        // adding a list to add items to
        items = new ArrayList<>();
        itemsAdapter = new MyAdapter(this, items, new MyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                showTaskCreatorChange(position);
            }
        });
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(itemsAdapter);

        // adding a divider between the todos
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        button.setOnClickListener(view -> addItem());

        // checkbox to have only my to-do's
        checkboxMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                itsMe = isChecked;
                updateRecyclerView();
            }
        });

        enableSwipeToDelete();
    }

    private void addItem() {
        EditText input = findViewById(R.id.edit_text);
        String itemText = input.getText().toString();
        String taskCreatorName = "Max Mustermann";

        if(!itemText.isEmpty()) {
            TodoItem todoItem = new MyAdapter.TodoItem(itemText, taskCreatorName, false, true);
            items.add(0,todoItem);
            itemsAdapter.notifyDataSetChanged();
            input.setText("");

            //update item counter
            itemCounter++;
            itemCountTextView.setText(String.valueOf(itemCounter));

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


                //update todoCounter
                itemCounter--;
                itemCountTextView.setText(String.valueOf(itemCounter));
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void removeItem(int position) {
        items.remove(position);
        itemsAdapter.notifyDataSetChanged();
        updateRecyclerView();
        Toast.makeText(getApplicationContext(), "Item removed", Toast.LENGTH_SHORT).show();
    }

    private void updateRecyclerView() {
        ArrayList<MyAdapter.TodoItem> filteredItems = new ArrayList<>();

        for(MyAdapter.TodoItem item : items) {
            if (!itsMe || !("Max Mustermann".equals(item.getTaskCreator()))) {
                filteredItems.add(item);
            }
        }

        itemsAdapter.setItems(filteredItems);
        itemsAdapter.notifyDataSetChanged();
    }


    private void showTaskCreatorChange(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Task Creator");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTaskCreator = input.getText().toString();
                if(!newTaskCreator.isEmpty()) {
                    items.get(position).setTaskCreator(newTaskCreator);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }




}