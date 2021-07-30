package com.goboomtown.supportsdk.model;

import org.json.JSONObject;

public class JourneyStageModel {

    private String      id;
    private String      stage;
    private String      journey_id;
    private int         position;
    private String      creator_user_id;
    private String      title;
    private String      created;
    private String      updated;

    public JourneyStageModel(JSONObject json) {
        id              = json.optString("id");
        stage           = json.optString("stage");
        journey_id      = json.optString("journey_id");
        position        = json.optInt("position");
        creator_user_id = json.optString("creator_user_id");
        title           = json.optString("title");
        created         = json.optString("created");
        updated         = json.optString("updated");
    }

}
