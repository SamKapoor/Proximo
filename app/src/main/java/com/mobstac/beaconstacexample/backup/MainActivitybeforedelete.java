package com.mobstac.beaconstacexample.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.BeaconstacReceiver;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.MSPlace;
import com.mobstac.beaconstac.core.PlaceSyncReceiver;
import com.mobstac.beaconstac.core.RuleSyncReceiver;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;
import com.mobstac.beaconstac.utils.MSException;
import com.mobstac.beaconstac.utils.MSLogger;
import com.mobstac.beaconstacexample.activity.DetailActivity;
import com.mobstac.beaconstacexample.R;
import com.mobstac.beaconstacexample.adapter.RulesAdapter;
import com.mobstac.beaconstacexample.db.DatabaseHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivitybeforedelete extends AppCompatActivity {

    private static final String TAG = MainActivitybeforedelete.class.getSimpleName();
    Cursor cursor;
    private ArrayList<MSBeacon> beacons = new ArrayList<MSBeacon>();

    //    private BeaconAdapter beaconAdapter;
    private TextView bCount;
    private TextView testCamped;
    Beaconstac bstacInstance;

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    private boolean registered = false;
    private boolean isPopupVisible = false;
    private RulesAdapter rulesAdapter;
    private ListView beaconList;
    ArrayList<MSAction> msActions = new ArrayList<>();
    private DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconList = (ListView) findViewById(R.id.beaconListView);
        // Use this check to determine whether BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            Toast.makeText(this, "Unable to obtain a BluetoothAdapter", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if (savedInstanceState == null) {
            initList();
        }

        // set region parameters (UUID and unique region identifier)
        bstacInstance = Beaconstac.getInstance(this);
        bstacInstance.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.mobstac.beaconstacexample");
        bstacInstance.syncRules();

        // if location is enabled
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            bstacInstance.syncPlaces();
            new PlaceSyncReceiver() {
                @Override
                public void onSuccess(Context context) {
                    bstacInstance.enableGeofences(true);
                    try {
                        bstacInstance.startRangingBeacons();
                    } catch (MSException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Context context) {
                    MSLogger.error("Error syncing geofence");
                }
            };

            // start scanning
            try {
                bstacInstance.startRangingBeacons();
            } catch (MSException e) {
                // handle for older devices
                TextView rangedView = (TextView) findViewById(R.id.RangedView);
                rangedView.setText(R.string.ble_not_supported);
                bCount.setVisibility(View.GONE);
                testCamped.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            // if location disabled, directly start ranging beacons
            try {
                bstacInstance.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }


        //            Get from db
        dbHandler = DatabaseHandler.getInstance(MainActivitybeforedelete.this);
        cursor = dbHandler.getRules();

        if (cursor != null && cursor.getCount() > 0) {
//            byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHandler.KEY_ACTION));
//            String json = new String(blob);
//            Gson gson = new Gson();
//            msActions = gson.fromJson(json, new TypeToken<ArrayList<MSAction>>() {
//            }.getType());
            rulesAdapter = new RulesAdapter(this, cursor, 0);
            beaconList.setAdapter(rulesAdapter);
        }

        beaconList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor1 = ((RulesAdapter) parent.getAdapter()).getCursor();
                cursor1.moveToPosition(position);
//                byte[] blob = cursor1.getBlob(cursor1.getColumnIndex(DatabaseHandler.KEY_ACTION));
//                String json = new String(blob);
//                Gson gson = new Gson();
//                msActions = gson.fromJson(json, new TypeToken<ArrayList<MSAction>>() {
//                }.getType());
//                Parcel parcel = Parcel.obtain();
//                parcel.readByteArray(blob);
//                parcel.readTypedList(msActions, MSAction.CREATOR);
//                msActions = getJavaObject(blob);
//                if (msActions != null && msActions.size() >= position) {

              /*  ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_IS_READ, "1");
                dbHandler.updateRule(values, cursor1.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_RULE_ID)));
                cursor = dbHandler.getRules();
                if (cursor != null && cursor.getCount() > 0) {
                    rulesAdapter.notifyDataSetChanged();
                }*/
                openRule(MainActivitybeforedelete.this, cursor1/*.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_NAME)), msActions*/);

//                }
            }
        });

//        Intent intent = getIntent();
//        if (intent.hasExtra("rulename")) {
//            openRule(this, intent.getStringExtra("rulename"), (ArrayList<MSAction>) intent.getSerializableExtra("actions"));
//        }

    }


    public byte[] getByteArrayObject(Parcel msAction) {

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
    public Parcel getJavaObject(byte[] convertObject) {
        Parcel objSimpleExample = null;

        ByteArrayInputStream bais;
        ObjectInputStream ins;
        try {

            bais = new ByteArrayInputStream(convertObject);

            ins = new ObjectInputStream(bais);
            objSimpleExample = (Parcel) ins.readObject();

            ins.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return objSimpleExample;
    }


    public void openRule(Context context, /*String ruleName,*/ Cursor cursor1 /*ArrayList<MSAction> actions*/) {
        HashMap<String, Object> messageMap;
        AlertDialog.Builder dialogBuilder;

        String title = cursor1.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_NAME));
        int type = cursor1.getInt(cursor1.getColumnIndex(DatabaseHandler.KEY_TYPE));
        String text = cursor1.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_TEXT));
        String webUrl = cursor1.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_WEB_URL));
        String mediaUrl = cursor1.getString(cursor1.getColumnIndex(DatabaseHandler.KEY_MEDIA_URL));

//        if (!isPopupVisible) {

        switch (type) {
            // handle action type Popup
            case 1:
                dialogBuilder = new AlertDialog.Builder(context);
//                        messageMap = action.getMessage();
                dialogBuilder.setTitle(title).setMessage(text);
                AlertDialog dialog = dialogBuilder.create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        isPopupVisible = false;
                    }
                });
                dialog.show();
                isPopupVisible = true;
                break;
            case 2:
                if (!isPopupVisible) {
                    dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            isPopupVisible = false;
                        }

                    });
                    dialogBuilder.setTitle(title);
                    final WebView webView = new WebView(context);
                    webView.loadUrl(webUrl);
                    webView.getSettings().setBuiltInZoomControls(true);
                    webView.setInitialScale(85);
                    dialogBuilder.setView(webView);
                    dialogBuilder.setPositiveButton("Close", null);
                    dialogBuilder.show();
                    isPopupVisible = true;
                }
                break;
            case 3:
                if (mediaUrl.contains(",http")) {
                    Intent intent = new Intent(MainActivitybeforedelete.this, DetailActivity.class);
                    intent.putExtra("images", mediaUrl);
                    startActivity(intent);
                } else {
                    Uri uri = Uri.parse(mediaUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
//                Uri uri = Uri.parse(mediaUrl);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//                dialogBuilder = new AlertDialog.Builder(context);
//                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        isPopupVisible = false;
//                    }
//
//                });
//                dialogBuilder.setTitle(title);
//                final WebView webView = new WebView(context);
//                webView.getSettings().setBuiltInZoomControls(true);
//                webView.loadUrl(mediaUrl);
//                webView.setInitialScale(85);
//                dialogBuilder.setView(webView);
//                dialogBuilder.setPositiveButton("Close", null);
//                dialogBuilder.show();
//
//                isPopupVisible = true;
                break;
        }

//            for (MSAction action : actions) {
//                messageMap = action.getMessage();
//                switch (action.getType()) {
//                    // handle action type Popup
//                    case MSActionTypePopup:
//                        dialogBuilder = new AlertDialog.Builder(context);
//                        messageMap = action.getMessage();
//                        dialogBuilder.setTitle(action.getName()).setMessage((String) messageMap.get("text"));
//                        AlertDialog dialog = dialogBuilder.create();
//                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                            @Override
//                            public void onDismiss(DialogInterface dialog) {
//                                isPopupVisible = false;
//                            }
//                        });
//                        dialog.show();
//                        isPopupVisible = true;
//                        break;
//                    // handle the action type Card
//                    case MSActionTypeCard:
//                        MSCard card = (MSCard) messageMap.get("card");
////                            switch (card.getType()) {
////                                case MSCardTypePhoto:
////                                case MSCardTypeMedia:
//                        ArrayList<MSMedia> mediaArray = card.getMediaArray();
//                        if (mediaArray.size() > 0) {
//                            MSMedia m = mediaArray.get(0);
//
//                            String src = m.getMediaUrl().toString();
//
//                            dialogBuilder = new AlertDialog.Builder(context);
//                            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    isPopupVisible = false;
//                                }
//
//                            });
//
//                            final WebView webView = new WebView(context);
//                            webView.loadUrl(src);
//
//                            dialogBuilder.setView(webView);
//                            dialogBuilder.setPositiveButton("Close", null);
//                            dialogBuilder.show();
//
//                            isPopupVisible = true;
//                        }
////                            }
//                        break;
//                    // handle action type webpage
//                    case MSActionTypeWebpage:
//                        if (!isPopupVisible) {
//                            dialogBuilder = new AlertDialog.Builder(context);
//                            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    isPopupVisible = false;
//                                }
//
//                            });
//
//                            final WebView webView = new WebView(context);
//                            webView.loadUrl(messageMap.get("url").toString());
//                            dialogBuilder.setView(webView);
//                            dialogBuilder.setPositiveButton("Close", null);
//                            dialogBuilder.show();
//                            isPopupVisible = true;
//                        }
//                        break;
//
//                }
//            }
//            Toast.makeText(getApplicationContext(), "Rule " + ruleName, Toast.LENGTH_SHORT).show();
//        }
    }

    private void initList() {
//        ListView beaconList = (ListView) findViewById(R.id.beaconListView);
//        beaconAdapter = new BeaconAdapter(beacons, this);
//        beaconList.setAdapter(beaconAdapter);
        bCount = (TextView) findViewById(R.id.beaconCount);
        testCamped = (TextView) findViewById(R.id.CampedView);
        registerBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        beaconAdapter.clear();
//        beaconAdapter.notifyDataSetChanged();
        bCount.setText("" + beacons.size());
        unregisterBroadcast();
        try {
            unregisterReceiver(ruleSyncReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isPopupVisible = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initList();
        bCount.setText("" + beacons.size());
        registerBroadcast();
        isPopupVisible = false;
        registerRuleReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        try {
            unregisterReceiver(ruleSyncReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // stop scanning when the app closes
//        if (bstacInstance != null) {
//            try {
//                bstacInstance.stopRangingBeacons();
//            } catch (MSException e) {
//                // handle for older devices
//                e.printStackTrace();
//            }
//        }

    }

    // Callback intent results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
        if (bstacInstance != null) {
            try {
                bstacInstance.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }
    }


    RuleSyncReceiver ruleSyncReceiver = new RuleSyncReceiver() {
        @Override
        public void onSuccess(Context context) {
            Log.d("Rules sync", "successful");
        }

        @Override
        public void onFailure(Context context) {
            Log.d("Rules sync", "failed");
        }
    };

    private void registerRuleReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RULE_SYNC_FAILURE);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RULE_SYNC_SUCCESS);
        try {
            registerReceiver(ruleSyncReceiver, intentFilter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private void registerBroadcast() {
        if (!registered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RANGED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_CAMPED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_BEACON);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RULE_TRIGGERED);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_ENTERED_REGION);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_REGION);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_ENTERED_GEOFENCE);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_GEOFENCE);
            registerReceiver(myBroadcastReceiver, intentFilter);
            registered = true;
        }
    }

    private void unregisterBroadcast() {
        if (registered) {
            unregisterReceiver(myBroadcastReceiver);
            registered = false;
        }
    }

    public static int getRandom(int number) {
        Random rand = new Random();
        return rand.nextInt(number);
    }

    BeaconstacReceiver myBroadcastReceiver = new BeaconstacReceiver() {
        @Override
        public void exitedBeacon(Context context, MSBeacon beacon) {
            testCamped.setText("Exited: " + beacon.getMajor() + ":" + beacon.getMinor());
//            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void rangedBeacons(Context context, ArrayList<MSBeacon> rangedBeacons) {
//            beaconAdapter.clear();
            bCount.setText("" + rangedBeacons.size());
            beacons.addAll(rangedBeacons);
//            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void campedOnBeacon(Context context, MSBeacon beacon) {
            testCamped.setText("Camped: " + beacon.getMajor() + ":" + beacon.getMinor());
//            beaconAdapter.addBeacon(beacon);
//            beaconAdapter.notifyDataSetChanged();
        }

        @Override
        public void triggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {
            Log.e("Trigger", "Rule triggered: " + ruleName);
//            Insert into DB
            DatabaseHandler dbHandler = DatabaseHandler.getInstance(MainActivitybeforedelete.this);
//            Gson gson = new Gson();

            HashMap<String, Object> messageMap;
            for (MSAction action : actions) {

                messageMap = action.getMessage();

                ContentValues values = new ContentValues();
                values.put(DatabaseHandler.KEY_NAME, action.getName());
                values.put(DatabaseHandler.KEY_RULE_ID, action.getRuleID());
                values.put(DatabaseHandler.KEY_IS_READ, "1");

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
                        }
                        values.put(DatabaseHandler.KEY_TYPE, 3);
                        break;
                }


////            values.put(DatabaseHandler.KEY_ACTION, gson.toJson(actions).getBytes());
//                Parcel parcel = Parcel.obtain();
//                parcel.writeTypedList(actions);
//                values.put(DatabaseHandler.KEY_ACTION, parcel.createByteArray());
                int count = dbHandler.getIfAvailable(action.getRuleID() + "", values.getAsInteger(DatabaseHandler.KEY_TYPE) + "");
                if (count == 0) {
                    long id = dbHandler.addRules(values);
                    Intent activityIntent = new Intent(context.getApplicationContext(), MainActivitybeforedelete.class);
                    //        activityIntent.putExtra("rulename", ruleName);
                    //        activityIntent.putExtra("actions", actions);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification mBuilder = new Notification.Builder(context.getApplicationContext())
                            .setContentText(ruleName)
                            .setContentTitle("Proximity")
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent).build();
                    NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(getRandom(56789), mBuilder);
                }
            }

            cursor = dbHandler.getRules();
            if (cursor != null && cursor.getCount() > 0) {
//                byte[] blob = cursor.getBlob(cursor.getColumnIndex(DatabaseHandler.KEY_ACTION));
//                String json = new String(blob);
//                msActions = gson.fromJson(json, new TypeToken<ArrayList<MSAction>>() {
//                }.getType());
                if (rulesAdapter == null) {
                    rulesAdapter = new RulesAdapter(MainActivitybeforedelete.this, cursor, 0);
                    beaconList.setAdapter(rulesAdapter);
                } else {
                    rulesAdapter.notifyDataSetChanged();
                }
            }

//            Get from db
//            byte[] blob = cursor.getBlob(cursor.getColumnIndex(MyProvider.KEY_DATA));
//            String json = new String(blob);
//            Gson gson = new Gson();
//            ArrayList<MSAction> msActions = gson.fromJson(json, new TypeToken<ArrayList<MSAction>>() {
//            }.getType());
//            HashMap<String, Object> messageMap;

            AlertDialog.Builder dialogBuilder;
            for (MSAction action : actions) {
                messageMap = action.getMessage();
//                    Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    Notification mBuilder = new Notification.Builder(context.getApplicationContext())
//                            .setContentText(ruleName)
//                            .setContentTitle("Proximity")
//                            .setSmallIcon(R.mipmap.ic_launcher)
//                            .setContentIntent(pendingIntent).build();
//                    NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                    notificationManager.notify(getRandom(56789), mBuilder);


                switch (action.getType()) {
                    // handle action type Popup
                    case MSActionTypePopup:
                        dialogBuilder = new AlertDialog.Builder(context);
                        messageMap = action.getMessage();
                        dialogBuilder.setTitle(action.getName())
                                .setMessage((String) messageMap.get("text"));
                        AlertDialog dialog = dialogBuilder.create();
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                isPopupVisible = false;
                            }
                        });
                        dialog.show();
                        isPopupVisible = true;
                        break;

                    // handle the action type Card
                    case MSActionTypeCard:
                        MSCard card = (MSCard) messageMap.get("card");

//                            switch (card.getType()) {
//                                case MSCardTypePhoto:
//                                case MSCardTypeMedia:
                        ArrayList<MSMedia> mediaArray = card.getMediaArray();
                        String src = "";
                        if (mediaArray.size() > 0) {
                            for (int i = 0; i < mediaArray.size(); i++) {
                                MSMedia m = mediaArray.get(i);
                                if (src.isEmpty())
                                    src += m.getMediaUrl().toString();
                                else
                                    src += "," + m.getMediaUrl().toString();
                            }

//                            dialogBuilder = new AlertDialog.Builder(context);
//                            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    isPopupVisible = false;
//                                }
//
//                            });
//                            dialogBuilder.setTitle(ruleName);
//                            final WebView webView = new WebView(context);
//                            webView.getSettings().setBuiltInZoomControls(true);
//                            webView.setInitialScale(85);
//                            webView.loadUrl(src);
//
//                            dialogBuilder.setView(webView);
//                            dialogBuilder.setPositiveButton("Close", null);
//                            dialogBuilder.show();
//
//                            isPopupVisible = true;
                        }
                        if (src.contains(",http")) {
                            Intent intent = new Intent(MainActivitybeforedelete.this, DetailActivity.class);
                            intent.putExtra("images", src);
                            startActivity(intent);
                        } else {
                            Uri uri = Uri.parse(src);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }

//                            }
                        break;

                    // handle action type webpage
                    case MSActionTypeWebpage:
                        if (!isPopupVisible) {
                            dialogBuilder = new AlertDialog.Builder(context);
                            dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    isPopupVisible = false;
                                }

                            });
                            dialogBuilder.setTitle(ruleName);

                            final WebView webView = new WebView(context);
                            if (messageMap.get("url").toString().startsWith("http"))
                                webView.loadUrl(messageMap.get("url").toString());
                            else
                                webView.loadUrl("http://" + messageMap.get("url").toString());
                            webView.getSettings().setBuiltInZoomControls(true);
                            webView.setInitialScale(85);
                            dialogBuilder.setView(webView);
                            dialogBuilder.setPositiveButton("Close", null);
                            dialogBuilder.show();

                            isPopupVisible = true;

                        }
                        break;

                }
            }
            Toast.makeText(getApplicationContext(), "Rule " + ruleName, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void enteredRegion(Context context, String region) {
//            beaconAdapter.clear();
//            beaconAdapter.notifyDataSetChanged();
            bCount.setText("" + beacons.size());
            Toast.makeText(getApplicationContext(), "Entered region", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void exitedRegion(Context context, String region) {
//            beaconAdapter.clear();
//            beaconAdapter.notifyDataSetChanged();
            bCount.setText("" + beacons.size());
            Toast.makeText(getApplicationContext(), "Exited region", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void enteredGeofence(Context context, ArrayList<MSPlace> places) {
            Toast.makeText(getApplicationContext(), "Entered Geofence " + places.get(0).getName(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void exitedGeofence(Context context, ArrayList<MSPlace> places) {
            Toast.makeText(getApplicationContext(), "Exited Geofence " + places.get(0).getName(), Toast.LENGTH_SHORT).show();
        }
    };
}
