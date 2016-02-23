package com.mobstac.beaconstacexample;

import com.mobstac.beaconstac.models.MSAction;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by admin on 11/22/2015.
 */
public class MSActionsSerializable extends MSAction implements Serializable {

    public MSActionsSerializable(int id, String actionName, int ruleID, String ruleName, int actionType, JSONObject meta, Date created, Date updated, String status) {
        super(id, actionName, ruleID, ruleName, actionType, meta, created, updated, status);
    }
}
