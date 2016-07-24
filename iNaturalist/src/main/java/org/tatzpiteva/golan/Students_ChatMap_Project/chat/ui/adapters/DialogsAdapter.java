package org.tatzpiteva.golan.Students_ChatMap_Project.chat.ui.adapters;

/**
 * Created by ta2er mosa
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;

import com.quickblox.users.model.QBUser;

import org.inaturalist.android.R;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.core.ChatService;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.UiUtils;

import java.util.List;

//import vc908.stickerfactory.StickersManager;

public class DialogsAdapter extends BaseAdapter {
    private List<QBDialog> dataSource;
    private LayoutInflater inflater;
    private Context context;
    public String unreadcounter="0";

    public DialogsAdapter(List<QBDialog> dataSource, Activity ctx) {
        this.dataSource = dataSource;
        this.inflater = LayoutInflater.from(ctx);
        this.context=ctx;
    }

    public List<QBDialog> getDataSource() {
        return dataSource;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    public String getX()
    {
        return "H";
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // initIfNeed view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_room, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.roomName);
            holder.lastMessage = (TextView)convertView.findViewById(R.id.lastMessage);
            holder.groupType = (TextView)convertView.findViewById(R.id.textViewGroupType);
            holder.roomImage=(ImageView)convertView.findViewById(R.id.roomImage);
            holder.unreadCounterTextView = (TextView) convertView.findViewById(R.id.text_dialog_unread_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        QBDialog dialog = dataSource.get(position);
        if(dialog.getType().equals(QBDialogType.GROUP)){
            holder.name.setText(dialog.getName());
        }else{
            // get opponent name for private dialog
            //

            Integer opponentID = ChatService.getInstance().getOpponentIDForPrivateDialog(dialog);
            QBUser user = ChatService.getInstance().getDialogsUsers().get(opponentID);
            if(user != null){
                holder.name.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
                holder.roomImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_person_white_24dp));
                holder.roomImage.setBackgroundDrawable(UiUtils.getColorCircleDrawable(position));
            }
        }
      //  if (dialog.getLastMessage() != null && StickersManager.isSticker(dialog.getLastMessage())) {
          //  holder.lastMessage.setText("Sticker");
       // } else {
        int unreadMessagesCount = dialog.getUnreadMessageCount();

        if (unreadMessagesCount == 0) {
            unreadcounter="0";
            holder.unreadCounterTextView.setVisibility(View.GONE);
        } else {
            holder.unreadCounterTextView.setVisibility(View.VISIBLE);
            holder.unreadCounterTextView.setText(String.valueOf(unreadMessagesCount > 99 ? 99 : unreadMessagesCount));
            unreadcounter=holder.unreadCounterTextView.getText().toString();
        }
            holder.lastMessage.setText(dialog.getLastMessage());
      //  }
        holder.groupType.setText(dialog.getType().toString());

        return convertView;
    }

    private static class ViewHolder{
        TextView name;
        TextView lastMessage;
        TextView groupType;
        ImageView roomImage ;
        TextView unreadCounterTextView;
    }
}
