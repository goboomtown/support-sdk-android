package com.goboomtown.supportsdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.minidns.record.A;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class KBViewModel {

    private static final int kMaxDepth = 1;

    private ArrayList<KBEntryModel> folders = new ArrayList<>();
    public  ArrayList<KBEntryModel> entries = new ArrayList<>();
    private ArrayList<KBEntryModel> visibleEntries = new ArrayList<>();
    private KBEntryModel            kbRoot;
    private int                     baseLevel;

    public KBViewModel() {
    }

    public KBViewModel(JSONArray entriesJSON) {
        processEntries(entriesJSON);
        if ( kbRoot != null ) {
            for (Object child : kbRoot.children()) {
                if (child instanceof KBEntryModel) {
                    folders.add((KBEntryModel) child);
                }
            }
        }
    }


    public KBViewModel(KBEntryModel rootEntry) {
        kbRoot = rootEntry;
        for ( Object child : kbRoot.children() ) {
            if ( child instanceof KBEntryModel ) {
                folders.add((KBEntryModel)child);
            }
        }
    }


    private void processEntries(JSONArray entriesJSON) {
        KBEntryModel parent = null;
        ArrayList<KBEntryModel> remainingEntries = new ArrayList<>();
        entries = new ArrayList<>();
        for ( int n=0; n<entriesJSON.length(); n++  ) {
            try {
                JSONObject entryJSON = entriesJSON.getJSONObject(n);
                KBEntryModel entry = new KBEntryModel(entryJSON);
                entries.add(entry);
                if ( entry.isRoot() ) {
                    entry.level = 0;
                    kbRoot = entry;
                    parent = entry;
                } else {
                    remainingEntries.add(entry);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        while ( remainingEntries.size() > 0 ) {
            if ( parent != null ) {
                remainingEntries = parent.addChildrenFromEntries(remainingEntries);
            }
        }
    }


    private void updateVisibleEntries() {
        baseLevel = kbRoot.level+1;;
        visibleEntries = new ArrayList<>();
        for ( Object entry : kbRoot.children() ) {
            addVisibleChildrenOfEntry((KBEntryModel)entry);
        }
    }


    private void addVisibleChildrenOfEntry(KBEntryModel baseEntry) {
        if ( (baseEntry.level-baseLevel) > kMaxDepth ) {
            return;
        }
        visibleEntries.add(baseEntry);
        if ( !baseEntry.isCollapsed() ) {
            for ( Object object : baseEntry.children() ) {
                if ( object instanceof  KBEntryModel ) {
                    KBEntryModel entry = (KBEntryModel) object;
                    if (entry.isFolder()) {
                        addVisibleChildrenOfEntry(entry);
                    } else {
                        visibleEntries.add(entry);
                    }
                }
            }
        }
    }


    public KBEntryModel folderAt(int section) {
        return folders.get(section);
    }


    public ArrayList<KBEntryModel>  folders() {
        return folders;
    }

    public ArrayList<String> folderHeadings() {
        ArrayList<String > headings = new ArrayList<>();
        for ( KBEntryModel folder : folders ) {
            headings.add(folder.title() );
        }
        return headings;
    }


    public HashMap<KBEntryModel, List<Object>> allEntries() {
        HashMap<KBEntryModel, List<Object>> list = new HashMap<> ();
        for ( KBEntryModel folder : folders) {
            list.put(folder, folder.articles());
        }
        return list;
    }


    public HashMap<String, List<Object>> allEntriesByFolderName() {
        HashMap<String, List<Object>> list = new HashMap<> ();
        for ( KBEntryModel folder : folders) {
            list.put(folder.title(), folder.articles());
        }
        return list;
    }


    public static class KBEntryModelComparator implements Comparator<KBEntryModel> {

        public KBEntryModelComparator() {
        }

        public int compare(KBEntryModel left, KBEntryModel right) {
            return left.title().compareTo(right.title());
        }
    }

}
