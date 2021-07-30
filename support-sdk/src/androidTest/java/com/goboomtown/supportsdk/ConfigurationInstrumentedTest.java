package com.goboomtown.supportsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.platform.app.InstrumentationRegistry;

import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.model.Configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConfigurationInstrumentedTest {

    String configJSONString = "{\n" +
            "  \"apiHost\": \"https://api.preprod.goboomtown.com\",\n" +
            "  \"integrationId\": \"ZEUNHK\",\n" +
            "  \"apiKey\": \"qWvbbg3knrhu62ZmTB5VlR9ysEHNdcDqJ2ra2uDBumnb\",\n" +
            "  \"buttonURL\":\"https://api.preprod.goboomtown.com/resources/images/sdk_button.png\",\n" +
            "  \"partnerToken\": \"F9E93D721E717B0F71B3\",\n" +
            "  \"privateKey\": \"afa9417b1ae37e18f32dd15844dc812dccdc30a5\"\n" +
            "}";
    Context appContext = null;
    Configuration configuration = null;


    @Before
    public void init() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        configuration = new Configuration(appContext, configJSONString);
    }

    @Test
    public void testAppContext() throws Exception {
         assertThat(appContext.getPackageName(), startsWith("com.goboomtown.supportsdk."));
    }


    @Test
    public void testSaveDeveloperMode() {
        configuration.enableDeveloperMode(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        boolean isDeveloperMode = prefs.getBoolean(SupportSDK.KEY_DEV_MODE_ENABLED, false);
        assertTrue(isDeveloperMode);
    }

    @Test
    public void testIsDeveloperMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        boolean isDeveloperMode = prefs.getBoolean(SupportSDK.KEY_DEV_MODE_ENABLED, false);
        assertEquals(isDeveloperMode, configuration.isDeveloperMode());
    }

    @Test
    public void testSaveConfiguration() {
        JSONObject configJSON = null;
        try {
            configJSON = new JSONObject(configJSONString);
            configuration.saveConfiguration();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        String savedJSON = prefs.getString(SupportSDK.KEY_DEV_MODE_JSON_CONFIG, null);
        assertEquals(configJSON.toString(), savedJSON);
    }


    @Test
    public void testClearSavedConfiguration() {
        configuration.clearSavedConfiguration();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        assertFalse(prefs.contains(SupportSDK.KEY_DEV_MODE_JSON_CONFIG));
    }


    @Test
    public void testGetSavedConfiguration() {
        configuration.saveConfiguration();
        JSONObject json = configuration.getSavedConfiguration();
        assertThat(configJSONString, is(json.toString()));
    }


}
