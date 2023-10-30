package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.todo_app.MyAdapter.TodoItem;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListItemsDone extends AppCompatActivity {

    private ArrayList<TodoItem> completedItems;
    private RecyclerView recyclerView2;
    private MyAdapter itemsAdapter;
    private DatabaseReference databaseReference;
    private Button buttonGoBack;
    private TextView emptyTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_items_done);

        buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(ListItemsDone.this, MainActivity.class);
                startActivity(intent);
            }
        });


        recyclerView2 = findViewById(R.id.recyclerView2);
        databaseReference = FirebaseDatabase.getInstance().getReference("items");

        emptyTextView = findViewById(R.id.emptyTextView);

        completedItems = new ArrayList<>();
        itemsAdapter = new MyAdapter(this, completedItems,null);
        checkIfEmpty();

        recyclerView2.setAdapter(itemsAdapter);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.addItemDecoration(new DividerItemDecoration(recyclerView2.getContext(), DividerItemDecoration.VERTICAL));

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MyAdapter.TodoItem todoItem = dataSnapshot.getValue(TodoItem.class);
                if (todoItem != null && todoItem.isCompleted()) {
                    completedItems.add(todoItem);
                    itemsAdapter.notifyDataSetChanged();
                    checkIfEmpty();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                MyAdapter.TodoItem updatedTodoItem = dataSnapshot.getValue(TodoItem.class);
                if(updatedTodoItem != null) {
                    for (int i = 0; i < completedItems.size(); i++) {
                        MyAdapter.TodoItem current = completedItems.get(i);
                        if(current.getKey().equals(updatedTodoItem.getKey())) {
                            completedItems.set(i, updatedTodoItem);
                            itemsAdapter.notifyDataSetChanged();
                            checkIfEmpty();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    }

    private void checkIfEmpty() {
        if (completedItems.isEmpty()) {
        emptyTextView.setVisibility(View.VISIBLE);
        } else {
        emptyTextView.setVisibility(View.GONE);
        }
    }


}
