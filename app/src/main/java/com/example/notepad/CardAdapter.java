package com.example.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {
    private List<NoteList.ListFormat> mList;
    private Context mContext ;
    public interface OnItemLongClickListener {
        void onClick(int position);
    }
    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener listener;

    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
    public CardAdapter(Context context,List<NoteList.ListFormat> list){
        this.mContext=context;
        this.mList=list;
    }
    public void removeData(int position){
        mList.remove(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.title.setText(mList.get(position).title);
            holder.content.setText(mList.get(position).content);
            holder.date.setText(mList.get(position).date);
            holder.content.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onClick(position);
                    }
                }
            });
            holder.content.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View view) {
                    if (longClickListener != null) {
                        longClickListener.onClick(position);
                    }
                    return true;
                }
            });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView content;
        private TextView date;
        public MyViewHolder(View itemView){
            super(itemView);
            title=itemView.findViewById(R.id.title);
            content=itemView.findViewById(R.id.content);
            date=itemView.findViewById(R.id.date);
        }
    }
}
