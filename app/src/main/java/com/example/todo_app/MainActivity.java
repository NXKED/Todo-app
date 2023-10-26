package com.example.todo_app;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;



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

        FirebaseApp.initializeApp(this);

        recyclerView = findViewById(R.id.recyclerView);
        button = findViewById(R.id.button);
        itemCountTextView = findViewById(R.id.todoCount);
        CheckBox checkboxMe = findViewById(R.id.checkboxMe);

        // adding a list to add items to
        items = new ArrayList<>();
        initializeAdapter();

        itemCountTextView.setText(String.valueOf(itemCounter));
        button.setOnClickListener(view -> addItem());

        // retreive Data from Firebase db

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                items.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    MyAdapter.TodoItem todoItem = snapshot.getValue(TodoItem.class);
                    if(todoItem != null) {
                        todoItem.setKey(key);
                        items.add(todoItem);
                    }
                }
                itemsAdapter.notifyDataSetChanged();
                updateRecyclerView();
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
        String taskCreatorName = "Max Mustermann";
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


    private class AddItemTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String itemText = params[0];
            String taskCreatorName = params[1];
            return performNetworkRequest(itemText, taskCreatorName);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                // The network request was successful, update the UI
                itemsAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the case when the request was not successful
                Toast.makeText(getApplicationContext(), "Failed to add item", Toast.LENGTH_LONG).show();
            }

        }
    }

    private boolean performNetworkRequest(String itemText, String taskCreatorName) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String json = "{ \"taskName\": \"" + itemText + "\", \"taskCreator\": \"" + taskCreatorName + "\", \"completed\": false, \"hasCheckbox\": true }";
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url("https://todo-app-db1-default-rtdb.europe-west1.firebasedatabase.app/items.json")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                Snackbar.make(findViewById(android.R.id.content), "DB Successfull", Snackbar.LENGTH_SHORT).show();
                return true;
            } else {
                Snackbar.make(findViewById(android.R.id.content), "DB Error", Snackbar.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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

}