package com.codekarhu.whatswithme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @author ${user} on ${date}
 * <p/>
 * ${file_name}
 */
public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {

    private static final int MAIN_TYPE = 0;

    private List<Message> chatMessageList = new ArrayList<Message>();

    private OnItemClickListener listener;
    private Context ctx;
    private SparseBooleanArray selectedItems;



    public ChatRecyclerAdapter(Context context) {
        this.ctx = context;
        selectedItems = new SparseBooleanArray();
        dp8 = Utils.dp(8);
    }

    public void add(Message object) {
        chatMessageList.add(object);
        notifyItemInserted(getItemCount());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == MAIN_TYPE) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item, parent, false);
            return new ViewHolder(v);
        }
        Log.wtf("recycler", "No type??? WTF??");
        return null;
    }

    int dp8;

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (chatMessageList == null) return;
        Message chatMessageObj = this.chatMessageList.get(position);

        switch (getItemType(position)) {
            default:

                ((ViewHolder) holder).msgText.setText(chatMessageObj.message);
                holder.itemView.setOnClickListener(this);
                holder.itemView.setActivated(selectedItems.get(position, false));
                //((ViewHolder)holder).parent.setActivated(selectedItems.get(position, false));

                if (!chatMessageObj.left) {//own ones
                    ((ViewHolder) holder).root.setGravity(Gravity.END);
                    ((ViewHolder) holder).root.setPadding(50, 10, 10, 10);
                    ((ViewHolder) holder).parent.setBackgroundResource(R.drawable.chat_outgoing_text_states);
                    ((ViewHolder) holder).msgText.setPadding(dp8, 0, dp8, 0);
                } else {
                    ((ViewHolder) holder).root.setGravity(Gravity.START);
                    ((ViewHolder) holder).root.setPadding(10, 10, 50, 10);
                    ((ViewHolder) holder).parent.setBackgroundResource(R.drawable.chat_incoming_text_states);
                    ((ViewHolder) holder).msgText.setPadding(dp8*2, 0, dp8, 0);
                }

                break;
        }
    }



    public int getItemType(int position) {
        return MAIN_TYPE;

    }

    @Override
    public int getItemCount() {
        return this.chatMessageList.size();
    }


    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onItemClick((RecyclerView.ViewHolder) v.getTag());
        }
    }

    public void addOrUpdate(String text) {
        Message message = chatMessageList.get(chatMessageList.size()-1);
        if(!message.left) {
            chatMessageList.get(chatMessageList.size()-1).message = text;
            notifyItemChanged(chatMessageList.size()-1);
        } else {
            add(new Message(false, text));
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView msgText;
        public TextView time;

        public LinearLayout root;
        public FrameLayout parent;

        public ImageView check;
        public ImageView halfCheck;

        public ViewHolder(View itemView) {
            super(itemView);

            msgText = (TextView) itemView.findViewById(R.id.msgText);
            root = (LinearLayout) itemView.findViewById(R.id.msgRoot);
            parent = (FrameLayout) itemView.findViewById(R.id.msgParent);

        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemType(position);
    }

    public interface OnItemClickListener{
        void onItemClick(RecyclerView.ViewHolder view);
    }

}
