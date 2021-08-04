package com.example.d_bee_7.ui;

import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.d_bee_7.R;
import com.example.d_bee_7.logic.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.litepal.LitePal;

import java.util.List;


public class NotesFragment extends Fragment {
    List<Note> noteList;
    RecyclerView recyclerView;
    NoteRecyclerViewAdapter noteRecyclerViewAdapter;
    FloatingActionButton floatingActionButton;

    public NotesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        noteList = LitePal.findAll(Note.class);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteRecyclerViewAdapter = new NoteRecyclerViewAdapter(noteList, fragmentTransaction);
        recyclerView.setAdapter(noteRecyclerViewAdapter);
        floatingActionButton = view.findViewById(R.id.floating_action_button);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fragmentTransaction.replace(R.id.fragment_container_view, new NoteFragment());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}