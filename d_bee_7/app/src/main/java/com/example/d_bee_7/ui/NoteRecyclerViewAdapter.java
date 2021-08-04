package com.example.d_bee_7.ui;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d_bee_7.MyApplication;
import com.example.d_bee_7.R;
import com.example.d_bee_7.logic.model.Note;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.List;


public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {
    private List<Note> noteList;
    private final FragmentTransaction fragmentTransaction;

    public NoteRecyclerViewAdapter(List<Note> noteList, FragmentTransaction fragmentTransaction) {
        this.noteList = noteList;
        this.fragmentTransaction = fragmentTransaction;
    }

    @NonNull
    @org.jetbrains.annotations.NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentTransaction.replace(R.id.fragment_container_view, new NoteFragment(noteList.get(position)));
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        holder.tv_title.setText(noteList.get(position).getTitle());
        holder.tv_content.setText(noteList.get(position).getContent());
        holder.tv_date.setText(noteList.get(position).getDate());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MyApplication.context, holder.itemView);
                popupMenu.getMenuInflater().inflate(R.menu.long_click_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                LitePal.delete(Note.class, noteList.get(position).getId());
                                noteList.remove(position);
                                setNoteList(noteList);
                                notifyDataSetChanged();
                                break;
                            case R.id.count_time:
                                addCalendarEvent(noteList.get(position));
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.setGravity(Gravity.END);
                popupMenu.show();
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        if (noteList == null)
            return 0;
        else
            return noteList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_title;
        public TextView tv_content;
        public TextView tv_date;

        public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
            super(itemView);
            tv_content = itemView.findViewById(R.id.tv_content);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_date = itemView.findViewById(R.id.tv_date);
        }
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    private void addCalendarEvent(Note note) {
        Calendar beginTime = Calendar.getInstance();//开始时间
        beginTime.clear();
        beginTime.set(2021, 8, 4, 19, 24);//2014年1月1日12点0分(注意：月份0-11，24小时制)
        Calendar endTime = Calendar.getInstance();//结束时间
        endTime.clear();
        endTime.set(2021, 8, 4, 19, 25);//2014年2月1日13点30分(注意：月份0-11，24小时制)
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Uri.parse("content://com.android.calendar/events"))
                .putExtra("beginTime", beginTime.getTimeInMillis())
                .putExtra("endTime", endTime.getTimeInMillis())
                .putExtra("title", note.getTitle())
                .putExtra("location","这是地点测试")
                .putExtra("description", note.getContent());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.context.startActivity(intent);
    }

}
