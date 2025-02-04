package org.thoughtcrime.securesms.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import org.session.libsession.utilities.NotificationPrivacyPreference;
import org.session.libsession.utilities.recipients.Recipient;

import network.qki.messenger.R;

public class FailedNotificationBuilder extends AbstractNotificationBuilder {

  public FailedNotificationBuilder(Context context, NotificationPrivacyPreference privacy, Intent intent) {
    super(context, privacy);

    setSmallIcon(R.drawable.ic_notification);
    setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                              R.drawable.ic_action_warning_red));
    setContentTitle(context.getString(R.string.MessageNotifier_message_delivery_failed));
    setContentText(context.getString(R.string.MessageNotifier_failed_to_deliver_message));
    setTicker(context.getString(R.string.MessageNotifier_error_delivering_message));
    setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE));
    setAutoCancel(true);
    setAlarms(null, Recipient.VibrateState.DEFAULT);
    setChannelId(NotificationChannels.FAILURES);
  }



}
