package com.mobstac.beaconstacexample.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.RuleSyncReceiver;

/**
 * Created by admin on 11/1/2015.
 */
public class SyyncRulesActivity extends Activity {

    private Beaconstac bstacInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bstacInstance = Beaconstac.getInstance(this);
        bstacInstance.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.mobstac.beaconstacexample");
        registerRuleReceiver();
        bstacInstance.syncRules();
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(ruleSyncReceiver);
        } catch (Exception e) {

        }
    }

    RuleSyncReceiver ruleSyncReceiver = new RuleSyncReceiver() {
        @Override
        public void onSuccess(Context context) {
            Log.d("Rules sync", "successful");
            startActivity(new Intent(SyyncRulesActivity.this, MainActivityBackup.class));
        }

        @Override
        public void onFailure(Context context) {
            Log.d("Rules sync", "failed");
        }
    };
}
