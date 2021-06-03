package com.goboomtown.supportsdk;

import android.content.Context;
import android.content.res.Resources;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.goboomtown.supportsdk.api.Appearance;
import com.goboomtown.supportsdk.util.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class AppearanceInstrumentedTest {

    @Test
    public void init() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Appearance appearance = new Appearance(appContext);
        assertNotNull(appearance);
    }

    @Test
    public void getJSONFromFile() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Resources mResources = appContext.getResources();
        String mPackageName = appContext.getPackageName();
        int id = mResources.getIdentifier("default_appearance", "raw", mPackageName);
        String jsonString = null;
        try {
            jsonString = Utils.readRawTextFile(appContext, id);
        } catch (Exception e) {
        }
        assertNotNull(jsonString);
    }

    @Test
    public void getJSONFromEmptyFile() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Resources mResources = appContext.getResources();
        String mPackageName = appContext.getPackageName();
        int id = mResources.getIdentifier("default_appearancex", "raw", mPackageName);
        String jsonString = null;
        try {
            jsonString = Utils.readRawTextFile(appContext, id);
        } catch (Exception e) {
        }
        assertNull(jsonString);
    }


    @Test
    public void configureFromJSON() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Resources mResources = appContext.getResources();
        String mPackageName = appContext.getPackageName();
        int id = mResources.getIdentifier("default_appearance", "raw", mPackageName);
        String jsonString = null;
        try {
            jsonString = Utils.readRawTextFile(appContext, id);
        } catch (Exception e) {
        }
        Appearance appearance = new Appearance(appContext);
        appearance.configureFromJSON(jsonString);
        assertNotNull(appearance.textColor());
    }

}
