package com.mobstac.beaconstacexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mobstac.beaconstac.core.BeaconstacReceiver;
import com.mobstac.beaconstac.core.MSPlace;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;
import com.mobstac.beaconstacexample.activity.MainActivity;
import com.mobstac.beaconstacexample.db.DatabaseHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class BeaconstacActionReceiver extends BeaconstacReceiver {
    private NotificationManager notificationManager;

    @Override
    public void exitedBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacActionReceiver.class.getName(), "exited called " + beacon.getBeaconKey());
        sendNotification(context, "Exited " + beacon.getMajor() + " : " + beacon.getMinor());
    }

    @Override
    public void rangedBeacons(Context context, ArrayList<MSBeacon> beacons) {
        Log.v(BeaconstacActionReceiver.class.getName(), "Ranged called " + beacons.size());
        sendNotification(context, "Ranged " + beacons.size() + " beacons");
    }

    @Override
    public void campedOnBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacActionReceiver.class.getName(), "camped on called " + beacon.getBeaconKey());
        sendNotification(context, "Camped " + beacon.getMajor() + " : " + beacon.getMinor());
    }

    @Override
    public void triggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {
        Log.v(BeaconstacActionReceiver.class.getName(), "triggered rule called " + ruleName);
//        DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
//        Gson gson = new Gson();
//        ContentValues values = new ContentValues();
//        values.put(DatabaseHandler.KEY_NAME, ruleName);
////        values.put(DatabaseHandler.KEY_ACTION, gson.toJson(actions).getBytes());
////        values.put(DatabaseHandler.KEY_ACTION, getByteArrayObject(actions));
//        Parcel parcel = Parcel.obtain();
//        parcel.writeTypedList(actions);
//        values.put(DatabaseHandler.KEY_ACTION, parcel.createByteArray());
//        long id = dbHandler.addRules(values);

        DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
//            Gson gson = new Gson();

        HashMap<String, Object> messageMap;
        for (MSAction action : actions) {

            messageMap = action.getMessage();

            ContentValues values = new ContentValues();
            values.put(DatabaseHandler.KEY_NAME, action.getName());
            values.put(DatabaseHandler.KEY_RULE_ID, action.getRuleID());
            values.put(DatabaseHandler.KEY_IS_READ, "0");

            switch (action.getType()) {
                // handle action type Popup
                case MSActionTypePopup:
                    values.put(DatabaseHandler.KEY_TYPE, 1);
                    values.put(DatabaseHandler.KEY_TEXT, (String) messageMap.get("text"));
                    break;
                case MSActionTypeWebpage:
                    values.put(DatabaseHandler.KEY_TYPE, 2);
                    values.put(DatabaseHandler.KEY_WEB_URL, messageMap.get("url").toString());
                    break;
                case MSActionTypeCard:
                    MSCard card = (MSCard) messageMap.get("card");
                    ArrayList<MSMedia> mediaArray = card.getMediaArray();
                    if (mediaArray.size() > 0) {
                        String src = "";
                        for (int i = 0; i < mediaArray.size(); i++) {
                            MSMedia m = mediaArray.get(i);
                            if (src.isEmpty())
                                src += m.getMediaUrl().toString();
                            else
                                src += "," + m.getMediaUrl().toString();
                        }
                        values.put(DatabaseHandler.KEY_MEDIA_URL, src);
//                        MSMedia m = mediaArray.get(0);
//                        String src = m.getMediaUrl().toString();
//                        values.put(DatabaseHandler.KEY_MEDIA_URL, src);
                    }
                    values.put(DatabaseHandler.KEY_TYPE, 3);
                    break;
            }
            int count = dbHandler.getIfAvailable(action.getRuleID() + "", values.getAsInteger(DatabaseHandler.KEY_TYPE) + "");

////            values.put(DatabaseHandler.KEY_ACTION, gson.toJson(actions).getBytes());
//                Parcel parcel = Parcel.obtain();
//                parcel.writeTypedList(actions);
//                values.put(DatabaseHandler.KEY_ACTION, parcel.createByteArray());
            if (count == 0) {
                long id = dbHandler.addRules(values);
                Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
                //        activityIntent.putExtra("rulename", ruleName);
                //        activityIntent.putExtra("actions", actions);
                PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification mBuilder = new Notification.Builder(context.getApplicationContext())
                        .setContentText(action.getName())
                        .setContentTitle("Proximity")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent).build();
                NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(getRandom(56789), mBuilder);

            }
            Intent intent = new Intent("custom-event-name");
            // You can also include some extra data.
            intent.putExtra("message", "This is my message!");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

    }

    public byte[] getByteArrayObject(ArrayList<MSAction> msAction) {

        byte[] byteArrayObject = null;
        try {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(msAction);

            oos.close();
            bos.close();
            byteArrayObject = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return byteArrayObject;
        }
        return byteArrayObject;
    }

    //converting byte[] to SimpleExample
    public ArrayList<MSAction> getJavaObject(byte[] convertObject) {
        ArrayList<MSAction> objSimpleExample = null;

        ByteArrayInputStream bais;
        ObjectInputStream ins;
        try {

            bais = new ByteArrayInputStream(convertObject);

            ins = new ObjectInputStream(bais);
            objSimpleExample = (ArrayList<MSAction>) ins.readObject();

            ins.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objSimpleExample;
    }

    public static int getRandom(int number) {
        Random rand = new Random();
        return rand.nextInt(number);
    }

    @Override
    public void enteredRegion(Context context, String region) {
        Log.v(BeaconstacActionReceiver.class.getName(), "Entered region " + region);
    }

    @Override
    public void exitedRegion(Context context, String region) {
        notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.v(BeaconstacActionReceiver.class.getName(), "Exited region " + region);
    }

    @Override
    public void enteredGeofence(Context context, ArrayList<MSPlace> arrayList) {
        Log.v(BeaconstacActionReceiver.class.getName(), "Entered geofence");
    }

    @Override
    public void exitedGeofence(Context context, ArrayList<MSPlace> arrayList) {
        Log.v(BeaconstacActionReceiver.class.getName(), "Exited geofence");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private void sendNotification(Context context, String text) {
//        if (context != null) {
//            Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(
//                    context.getApplicationContext(),
//                    0,
//                    activityIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//            );
//            Notification mBuilder = new Notification.Builder(context.getApplicationContext())
//                    .setContentText(text)
//                    .setContentTitle("BeaconstacExample")
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentIntent(pendingIntent).build();
//            notificationManager = (NotificationManager)
//                    context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(1, mBuilder);
//        }
    }
}
