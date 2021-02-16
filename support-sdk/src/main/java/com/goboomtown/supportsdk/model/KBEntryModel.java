package com.goboomtown.supportsdk.model;

import org.json.JSONObject;

import java.util.ArrayList;

public class KBEntryModel {

    public enum KBType {
        ARTICLE,
        FOLDER,
        LIBRARY
    };

    private String  title;
    private String  body;
    private String  created;
    private String  folderName;
    private String  id;
    private String  keywords;
    private String  ownerPartnerId;
    private String  parentId;
    private String  rootName;
    private String  rootParentId;
    private String  searchable;
    private String  shortName;
    private String  status;
    private String  subtitle;
    private int     type;
    private String  visibility;
    private String  avatar;
    private String  preview;
    private String  url;
    private String  desc;
    private String  path;
    private String  actual_path;
    public  int     level;

    private boolean     collapsed = true;

    private ArrayList<Object> children = new ArrayList<>();

    public KBEntryModel() {
    }

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
        searchable     = entryJSON.optString("searchable");
        shortName      = entryJSON.optString("short_name");
        status         = entryJSON.optString("status");
        subtitle       = entryJSON.optString("sub_title");
        title          = entryJSON.optString("title");
        type           = entryJSON.optInt("type", 0);
        visibility     = entryJSON.optString("visibility");
        avatar         = entryJSON.optString("avatar");
        preview        = entryJSON.optString("preview");
        url            = entryJSON.optString("url");
        desc           = entryJSON.optString("desc");
        path           = entryJSON.optString("path");
        actual_path    = entryJSON.optString("actual_path");
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
        ArrayList<KBEntryModel> remainingEntries = new ArrayList<>(entries);
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
        return type==KBType.FOLDER.ordinal() || type==KBType.LIBRARY.ordinal();
    }

    public boolean isArticle() {
        return type==KBType.ARTICLE.ordinal();
    }

    public boolean isRoot() {
        return type==KBType.LIBRARY.ordinal();
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

    public String url() {
        return url;
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
