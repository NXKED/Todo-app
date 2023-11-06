package com.example.todo_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllDoneItems extends AppCompatActivity {

    private ArrayList<MyAdapter.TodoItem> completedItems;
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
                Intent intent = new Intent(AllDoneItems.this, MainActivity.class);
                startActivity(intent);
            }
        });

        recyclerView2 = findViewById(R.id.recyclerView2);
        databaseReference = FirebaseDatabase.getInstance().getReference("Items");

        emptyTextView = findViewById(R.id.emptyTextView);

        completedItems = new ArrayList<>();
        itemsAdapter = new MyAdapter(this, completedItems, null);

        recyclerView2.setAdapter(itemsAdapter);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        recyclerView2.addItemDecoration(new DividerItemDecoration(recyclerView2.getContext(), DividerItemDecoration.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                completedItems.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MyAdapter.TodoItem todoItem = snapshot.getValue(MyAdapter.TodoItem.class);

                    if (todoItem != null && todoItem.isCompleted()) {
                        completedItems.add(todoItem);
                    }
                }
                itemsAdapter.notifyDataSetChanged();
                checkIfEmpty();
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
            emptyTextView.setVisibility(View.INVISIBLE);
        }
    }
}
