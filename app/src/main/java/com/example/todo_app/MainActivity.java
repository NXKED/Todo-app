package com.example.todo_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo_app.MyAdapter.TodoItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import java.util.ArrayList;





public class MainActivity extends AppCompatActivity {

    private ArrayList<MyAdapter.TodoItem> items;
    private RecyclerView recyclerView;
    private RecyclerView recyclerView3;
    private Button button;
    private Button buttonGoToAll;

    private MyAdapter itemsAdapter;
    private TextView itemCountTextView;
    private int itemCounter = 0;
    boolean itsMe = false;
    private String taskCreatorName = "Max M.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        recyclerView = findViewById(R.id.recyclerView);
        button = findViewById(R.id.button);
        button.setOnClickListener(view -> addItem());

        itemCountTextView = findViewById(R.id.todoCount);
        CheckBox checkboxMe = findViewById(R.id.checkboxMe);

        // adding a list to add items to
        items = new ArrayList<>();
        initializeAdapter();

        //List2 (completed Items List)
        recyclerView3 = findViewById(R.id.recyclerView3);
        buttonGoToAll = findViewById(R.id.buttonGoToAll);

        // onclick to new activity ListItemsDone view
        buttonGoToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListItemsDone.class);
                startActivity(intent);
            }
        });

        //Implement enter button to add Item
        EditText input = findViewById(R.id.edit_text);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addItem();
                    return true;
                }
                return false;
            }
        });


        //Settings -> ViewSettings

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeTaskCreatorPopUp();
            }
        });



        itemCountTextView.setText(String.valueOf(itemCounter));

        // retreive Data from Firebase db

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                itemCounter = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    MyAdapter.TodoItem todoItem = snapshot.getValue(TodoItem.class);
                    if(todoItem != null) {
                        todoItem.setKey(key);
                        items.add(todoItem);
                        if(!todoItem.isCompleted()) {
                            itemCounter++;
                        }
                    }
                }
                itemsAdapter.notifyDataSetChanged();
                updateRecyclerView();
                itemCountTextView.setText(String.valueOf(itemCounter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(findViewById(android.R.id.content), "Database Error", Snackbar.LENGTH_SHORT).show();
            }
        });




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

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("items");

        if (!itemText.isEmpty()) {
            DatabaseReference newItemReference = databaseReference.push();
            String newItemKey = newItemReference.getKey();

            if (newItemKey != null) {
                TodoItem todoItem = new MyAdapter.TodoItem(itemText, taskCreatorName, false, true);

                if (items.isEmpty()) {
                    items = new ArrayList<>();
                }
                items.add(0, todoItem);
                itemsAdapter.notifyDataSetChanged();
                input.setText("");

                //update item counter
                itemCounter++;
                itemCountTextView.setText(String.valueOf(itemCounter));

                newItemReference.setValue(todoItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Snackbar.make(findViewById(android.R.id.content), "Item added successfully", Snackbar.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(findViewById(android.R.id.content), "DB Error", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Snackbar.make(findViewById(android.R.id.content), "DB Error: newItemKey is null", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "No Text entered", Snackbar.LENGTH_LONG).show();
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

        if (items.isEmpty() || position < 0 || position >= items.size()) {
            Log.e("RemoveItem", "Invalid position");
            return;
        }

        MyAdapter.TodoItem itemToRemove = items.get(position);

        if(itemToRemove == null) {
            Snackbar.make(findViewById(android.R.id.content), "ItemToRemove is null", Snackbar.LENGTH_LONG).show();
            return;
        }

        String keyToRemove = itemToRemove.getKey();

        // remove from database

        if (keyToRemove != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference itemReference = databaseReference.child("items").child(itemToRemove.getKey());
            itemReference.removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (position < items.size()) {
                            items.remove(position);
                            itemsAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("RemoveItem", "Invalid pos");
                            }
                            Toast.makeText(getApplicationContext(), "Item removed", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception ex) {
                            Toast.makeText(getApplicationContext(), "Failed to remove item", Toast.LENGTH_LONG).show();
                        }
                    });
        }

       // Toast.makeText(getApplicationContext(), "Invalid item or key", Toast.LENGTH_SHORT).show();
    }

    private void updateRecyclerView() {
        ArrayList<MyAdapter.TodoItem> filteredItems = new ArrayList<>();

        for(MyAdapter.TodoItem item : items) {
            if (itsMe) {
                if (taskCreatorName.equals(item.getTaskCreator())) {
                filteredItems.add(item);
                }
            } else {
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
                    updateTaskCreatorName(newTaskCreator, items.get(position).getKey());
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


    private void initializeAdapter(){
        itemsAdapter = new MyAdapter(this, items, new MyAdapter.OnItemLongClickListener(){
            @Override
            public void onItemLongClick(int position) {
                showTaskCreatorChange(position);
            }
        });
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // adding a divider between the todos
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }

    private void updateTaskCreatorName (String newTaskCreator, String itemKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("items").child(itemKey);
        databaseReference.child("taskCreator").setValue(newTaskCreator)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Snackbar.make(findViewById(android.R.id.content), "Task Creator updated", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to update Creator Name", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChangeTaskCreatorPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change your name");

        //Edit Text field for user to change the name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newTaskCreator = input.getText().toString();
                if (!newTaskCreator.isEmpty()) {
                    taskCreatorName = newTaskCreator;

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