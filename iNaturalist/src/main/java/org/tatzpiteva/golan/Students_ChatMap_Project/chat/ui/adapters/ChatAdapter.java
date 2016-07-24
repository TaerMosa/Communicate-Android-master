package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;

import com.quickblox.users.model.QBUser;

import org.inaturalist.android.R;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.StickyListHeadersAdapter;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.ResourceUtils;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.TimeUtils;

import java.util.List;



public class ChatAdapter extends BaseAdapter implements StickyListHeadersAdapter{

    private final List<QBChatMessage> chatMessages;
    private Activity context;

    private enum ChatItemType {
        Message,
        Sticker
    }

    public ChatAdapter(Activity context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public QBChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ChatItemType.values().length;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        QBChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            if (getItemViewType(position) == ChatItemType.Sticker.ordinal()) {
                convertView = vi.inflate(R.layout.list_item_sticker, parent, false);
            } else {
                convertView = vi.inflate(R.layout.list_item_message, parent, false);
            }
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBUser currentUser = ChatService.getInstance().getCurrentUser();
        boolean isOutgoing = chatMessage.getSenderId() == null || chatMessage.getSenderId().equals(currentUser.getId());
        setAlignment(holder, isOutgoing);

     if (holder.txtMessage != null) {
            holder.txtMessage.setText(chatMessage.getBody());
        }

        return convertView;
    }

    public void add(QBChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<QBChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isOutgoing) {
        if (!isOutgoing) {
            holder.txtMessage.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
            if (holder.txtMessage != null) {
                holder.txtMessage.setTextSize(20);

                holder.contentWithBG.setBackgroundResource(R.drawable.outgoing_message_bg);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);

            } else {
                holder.contentWithBG.setBackgroundResource(android.R.color.transparent);
            }
        } else {
            holder.txtMessage.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);

            if (holder.txtMessage != null) {
                holder.txtMessage.setTextSize(20);
                holder.contentWithBG.setBackgroundResource(R.drawable.incoming_message_bg);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtMessage.setLayoutParams(layoutParams);
            } else {
                holder.contentWithBG.setBackgroundResource(android.R.color.transparent);
            }
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.stickerView = (ImageView) v.findViewById(R.id.sticker_image);
        return holder;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = vi.inflate(R.layout.view_chat_message_header, parent, false);
            holder.dateTextView = (TextView) convertView.findViewById(R.id.header_date_textview);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        holder.dateTextView.setText(TimeUtils.getDate(chatMessage.getDateSent() * 1000));

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.dateTextView.getLayoutParams();
        if (position == 0) {
            lp.topMargin = ResourceUtils.getDimen(R.dimen.chat_date_header_top_margin);
        } else {
            lp.topMargin = 0;
        }
        holder.dateTextView.setLayoutParams(lp);

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
    }




    private static class HeaderViewHolder {
        public TextView dateTextView;
    }
    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
        public ImageView stickerView;
    }
}
