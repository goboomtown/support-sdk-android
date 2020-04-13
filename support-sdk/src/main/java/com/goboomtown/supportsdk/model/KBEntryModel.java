package com.goboomtown.supportsdk.model;

import org.json.JSONObject;

import java.util.ArrayList;

public class KBEntryModel {

    private String    title;
    private String    body;
    private String    created;
    private String    folderName;
    private String    id;
    private String    keywords;
    private String    ownerPartnerId;
    private String    parentId;
    private String    rootName;
    private String    rootParentId;
    private boolean   searchable;
    private String    shortName;
    private String    status;
    private String    subtitle;
    private String    type;
    private String    visibility;
    public  int     level;

    private boolean     collapsed = true;

    private ArrayList<Object> children = new ArrayList<>();

    public KBEntryModel(JSONObject entryJSON) {
        this.title = title;
        this.body  = body;

        body           = entryJSON.optString("body");
        created        = entryJSON.optString("created");
        folderName     = entryJSON.optString("folder_name");
        id             = entryJSON.optString("id");
        keywords       = entryJSON.optString("keywords");
        ownerPartnerId = entryJSON.optString("owner_partner_id");
        parentId       = entryJSON.optString("parent_id");
        rootName       = entryJSON.optString("root_name");
        rootParentId   = entryJSON.optString("root_parent_id");
        searchable     = entryJSON.optBoolean("searchable", false);
        shortName      = entryJSON.optString("short_name");
        status         = entryJSON.optString("status");
        subtitle       = entryJSON.optString("sub_title");
        title          = entryJSON.optString("title");
        type           = entryJSON.optString("type");
        visibility     = entryJSON.optString("visibility");
    }


    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public boolean isCollapsed()
    {
        return collapsed;
    }


    public void addEntry(KBEntryModel entry) {
        children.add(entry);
    }

    public void addChild(KBEntryModel entry) {
        children.add(entry);
    }


    public  ArrayList<KBEntryModel> addChildrenFromEntries(ArrayList<KBEntryModel> entries) {
        ArrayList<KBEntryModel> remainingEntries = new ArrayList<KBEntryModel>(entries);
        for ( KBEntryModel entry : entries ) {
            if (  entry.parentId.equalsIgnoreCase(id) ) {
                entry.level = level + 1;
                addChild(entry);
                remainingEntries.remove(entry);
            } else {
                for ( Object child : children ) {
                    remainingEntries = ((KBEntryModel)child).addChildrenFromEntries(remainingEntries);
                }
            }
        }
        return remainingEntries;
    }


    public boolean isFolder() {
        return type.equalsIgnoreCase("Folder");
    }


    public boolean isArticle() {
        return type.equalsIgnoreCase("Article");
    }

    public boolean isRoot() {
        return rootParentId.isEmpty();
    }


    public ArrayList<Object> articles()
    {
        return children;
    }


    public String folderName() {
        return folderName;
    }

    public String title() {
        return title;
    }


    public String body() {
        return body;
    }


    public String id() {
        return id;
    }

    public String parentId() {
        return parentId;
    }

    public ArrayList<Object> children() {
        return children;
    }



}
