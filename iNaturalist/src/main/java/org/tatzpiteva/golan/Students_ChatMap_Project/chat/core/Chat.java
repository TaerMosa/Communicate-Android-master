package org.tatzpiteva.golan.Students_ChatMap_Project.chat.core;

import com.quickblox.chat.model.QBChatMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public interface Chat {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;
}
