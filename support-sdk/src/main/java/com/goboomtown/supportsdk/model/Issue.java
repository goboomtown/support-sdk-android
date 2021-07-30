package com.goboomtown.supportsdk.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by larry on 2016-07-21.
 */
public class Issue
{
    private static final String kSupportSDKCurrentIssue     = "supportSDKCurrentIssue";

    public final static int NONE = 0;    // R.id.issue_new;
    public static final int PREPARING = 1;
    public static final int REMOTE = 2;
    public static final int DISPATCHING = 3;
    public static final int SCHEDULED = 4;
    public final static int EN_ROUTE = 5;
    public final static int ON_SITE = 6;
    public final static int RESOLVED = 7;
    public static final int CLOSED = 8;
    public static final int PENDING_CLOSED = 9;
    public static final int CLOSED_ALSO = 10;
    public static final int CANCELLED = 89;

    public static final int kIssueResolutionNotSet      = 0;
    public static final int kIssueResolutionCompleted   = 1;
    public static final int kIssueResolutionChanged     = 2;
    public static final int kIssueResolutionCanceled    = 3;
    public static final int kIssueResolutionUnresolved  = 4;


    private static final String k_arrival_time              = "arrival_time";
    private static final String k_category 				    = "category";
    private static final String k_created 				    = "created";
    private static final String k_resolved 				    = "resolved";
    private static final String k_departure_time 		    = "departure_time";
    private static final String k_details 				    = "details";
    private static final String k_enroute_time			    = "enroute_time";
    private static final String k_id 					    = "id";
    private static final String k_job 					    = "job";
    private static final String k_members_email 			= "members_email";
    private static final String k_members_id				= "members_id";
    private static final String k_members_locations_id 	    = "members_locations_id";
    private static final String k_members_locations_name    = "members_locations_name";
    private static final String k_members_name 			    = "members_name";
    private static final String k_members_users_email 	    = "members_users_email";
    private static final String k_members_users_id 		    = "members_users_id";
    private static final String k_members_users_name 	    = "members_users_name";
    private static final String k_reference_num 			= "reference_num";
    private static final String k_remote_id 				= "remote_id";
    private static final String k_resolution 			    = "resolution";
    private static final String k_scheduled_time			= "scheduled_time";
    private static final String k_owner_user_avatar			= "owner_user_avatar";
    private static final String k_status 				    = "status";
    private static final String k_rating 				    = "rating";
    private static final String k_is_rated 				    = "isRated";
    private static final String k_type 					    = "type";
    private static final String k_updated 				    = "updated";
    private static final String k_comm_id                   = "comm_id";
    private static final String k_xmpp_data				    = "xmpp_data";

    public String   arrival_time;
    public String   category;
    public String   created;
    public String   resolved;
    public String   departure_time;
    public String   details;
    public String   enroute_time;
    public String   id;
    public String   job;
    public String   members_email;
    public String   members_id;
    public String   members_locations_id;
    public String   members_locations_name;
    public String   members_name;
    public String   members_users_email;
    public String   members_users_id;
    public String   members_users_name;
    public String   reference_num;
    public String   remote_id;
    public String   resolution;
    public String   scheduled_time;
    public String   owner_user_avatar;
    public int      status;
    public int      rating;
    public String   type;
    public String   updated;
    public String   comm_id;
    public String   xmpp_data;
    public JSONObject   transcripts;
    public boolean  isRated;


    public Issue() {
        clear();
    }

    public Issue(JSONObject issueJSON) {
        clear();
        populateFromJSON(issueJSON);
    }

    private void populateFromJSON(JSONObject issueJSON) {
        arrival_time 			=   issueJSON.optString(k_arrival_time);
        category 				=   issueJSON.optString(k_category);
        created 				=   issueJSON.optString(k_created);
        resolved 				=   issueJSON.optString(k_resolved);
        departure_time 		    =   issueJSON.optString(k_departure_time);
        details 				=   issueJSON.optString(k_details);
        enroute_time			=   issueJSON.optString(k_enroute_time);
        id 					    =   issueJSON.optString(k_id);
        job 					=   issueJSON.optString(k_job);
        members_email 			=   issueJSON.optString(k_members_email);
        members_id				=   issueJSON.optString(k_members_id);
        members_locations_id 	=   issueJSON.optString(k_members_locations_id);
        members_locations_name	=   issueJSON.optString(k_members_locations_name);
        members_name 			=   issueJSON.optString(k_members_name);
        members_users_email 	=   issueJSON.optString(k_members_users_email);
        members_users_id 		=   issueJSON.optString(k_members_users_id);
        members_users_name 	    =   issueJSON.optString(k_members_users_name);
        reference_num 			=   issueJSON.optString(k_reference_num);
        remote_id 				=   issueJSON.optString(k_remote_id);
        resolution 			    =   issueJSON.optString(k_resolution);
        scheduled_time			=   issueJSON.optString(k_scheduled_time);
        owner_user_avatar	    =   issueJSON.optString(k_owner_user_avatar);
        status 				    =   issueJSON.optInt(k_status, 0);
        rating 				    =   issueJSON.optInt(k_rating, 0);
        isRated                 =   issueJSON.optBoolean(k_is_rated, false);
        type 					=   issueJSON.optString(k_type);
        updated 				=   issueJSON.optString(k_updated);
        comm_id 				=   issueJSON.optString(k_comm_id);
        xmpp_data				=   issueJSON.optString(k_xmpp_data);
        transcripts             =   issueJSON.optJSONObject("transcripts");
    }

    private String toJSON() {
        JSONObject issueJSON = new JSONObject();
        String jsonString = null;
        try {
            issueJSON.put(k_arrival_time,           arrival_time);
            issueJSON.put(k_category,               category);
            issueJSON.put(k_created,                created);
            issueJSON.put(k_resolved,               resolved);
            issueJSON.put(k_departure_time,         departure_time);
            issueJSON.put(k_details,                details);
            issueJSON.put(k_enroute_time,           enroute_time);
            issueJSON.put(k_id,                     id);
            issueJSON.put(k_reference_num,          reference_num);
            issueJSON.put(k_job,                    job);
            issueJSON.put(k_members_email,          members_email);
            issueJSON.put(k_members_id,             members_id);
            issueJSON.put(k_members_locations_id,   members_locations_id);
            issueJSON.put(k_members_locations_name, members_locations_name);
            issueJSON.put(k_members_users_email,    members_users_email);
            issueJSON.put(k_members_users_id,       members_users_id);
            issueJSON.put(k_members_users_name,     members_users_name);
            issueJSON.put(k_reference_num,          reference_num);
            issueJSON.put(k_remote_id,              remote_id);
            issueJSON.put(k_resolution,             resolution);
            issueJSON.put(k_scheduled_time,         scheduled_time);
            issueJSON.put(k_status,                 status);
            issueJSON.put(k_rating,                 rating);
            issueJSON.put(k_is_rated,               isRated);
            issueJSON.put(k_type,                   type);
            issueJSON.put(k_updated,                updated);
            issueJSON.put(k_comm_id,                comm_id);
            issueJSON.put(k_xmpp_data,              xmpp_data);
            jsonString = issueJSON.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }


    private void clear() {
        arrival_time 			=   null;
        category 				=   null;
        created 				=   null;
        resolved                =   null;
        departure_time 		    =   null;
        details 				=   null;
        enroute_time			=   null;
        id 					    =   null;
        job 					=   null;
        members_email 			=   null;
        members_id				=   null;
        members_locations_id 	=   null;
        members_locations_name	=   null;
        members_name 			=   null;
        members_users_email 	=   null;
        members_users_id 		=   null;
        members_users_name 	    =   null;
        reference_num 			=   null;
        remote_id 				=   null;
        resolution 			    =   null;
        scheduled_time			=   null;
        owner_user_avatar       =   null;
        status 				    =   0;
        rating                  =   0;
        isRated                 =   false;
        type 					=   null;
        updated 				=   null;
        comm_id                 =   null;
        xmpp_data				=   null;
    }


    public static void saveCurrentIssue(Context context, Issue issue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(kSupportSDKCurrentIssue, issue.toJSON());
        editor.apply();     // This line is IMPORTANT !!!
    }


    public static void clearCurrentIssue(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(kSupportSDKCurrentIssue);
        editor.apply();     // This line is IMPORTANT !!!
    }


    public static Issue getCurrentIssue(Context context) {
        Issue issue = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = prefs.getString(kSupportSDKCurrentIssue,"");
        if ( !jsonString.isEmpty() ) {
            JSONObject json;
            try {
                json = new JSONObject(jsonString);
                issue = new Issue();
                issue.populateFromJSON(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return issue;
    }

    public boolean isOpen() {
        return status < RESOLVED;
    }


    public boolean isResolved() {
         return status == RESOLVED;
    }


    public boolean isClosed() {
        return status > RESOLVED;
    }

}


