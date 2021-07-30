package com.goboomtown.supportsdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.minidns.record.A;

import java.util.ArrayList;
import java.util.Iterator;

public class JourneyModel {

    private String  id;
    private String  partners_id;
    private String  creator_user_id;
    public  String  title;
    private String  desc;
    private String  data;
    private int     status;
    private String  created;
    private String  updated;
    private String  customer_id;
    private String  customer_location_id;
    private String  issue_id;
    public  String  journey_url;

    private ArrayList<JourneyStageModel>    stages;

    public JourneyModel(JSONObject json) {
        id              = json.optString("id");
        partners_id     = json.optString("partners_id");
        desc            = json.optString("description");
        status          = json.optInt("status");
        creator_user_id = json.optString("creator_user_id");
        title           = json.optString("title");
        created         = json.optString("created");
        updated         = json.optString("updated");

        customer_id            = json.optString("customer_id");
        customer_location_id   = json.optString("customer_location_id");
        issue_id               = json.optString("issue_id");
        journey_url            = json.optString("journey_url");

        stages = new ArrayList<>();
        try {
            JSONArray stagesJSON = json.optJSONArray("stages");
            if ( stagesJSON != null ) {
                for ( int n=0; n<stagesJSON.length(); n++ ) {
                    JourneyStageModel stageModel = new JourneyStageModel((JSONObject)stagesJSON.get(n));
                    stages.add(stageModel);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
