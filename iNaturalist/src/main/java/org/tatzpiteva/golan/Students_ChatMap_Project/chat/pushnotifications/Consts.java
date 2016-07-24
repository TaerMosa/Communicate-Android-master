package org.tatzpiteva.golan.Students_ChatMap_Project.chat.pushnotifications;

import org.inaturalist.android.R;
import org.tatzpiteva.golan.Students_ChatMap_Project.chat.utils.ResourceUtils;

/**
 * Created by ta2er mosa
 */
public class Consts {
    // In GCM, the Sender ID is a project ID that you acquire from the API console
    public static final String PROJECT_NUMBER = "205593179289";
    public static String GCM_SENDER_ID = "205593179289";
    public static final String EXTRA_MESSAGE = "message";

    public static final String GCM_NOTIFICATION = "GCM Notification";
    public static final String GCM_DELETED_MESSAGE = "Deleted messages on server: ";
    public static final String GCM_INTENT_SERVICE = "GcmIntentService";
    public static final String GCM_SEND_ERROR = "Send error: ";
    public static final String GCM_RECEIVED = "Received: ";

    public static final String NEW_PUSH_EVENT = "new-push-event";

   public static int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
   public static int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);
}
