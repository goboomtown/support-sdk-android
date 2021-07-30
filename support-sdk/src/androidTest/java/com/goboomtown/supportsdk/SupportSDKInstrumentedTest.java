package com.goboomtown.supportsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.platform.app.InstrumentationRegistry;

import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.view.SupportButton;

import org.json.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.*;

public class SupportSDKInstrumentedTest {

    String configJSONString = "{\n" +
            "  \"apiHost\": \"https://api.preprod.goboomtown.com\",\n" +
            "  \"integrationId\": \"ZEUNHK\",\n" +
            "  \"apiKey\": \"qWvbbg3knrhu62ZmTB5VlR9ysEHNdcDqJ2ra2uDBumnb\",\n" +
            "  \"buttonURL\":\"https://api.preprod.goboomtown.com/resources/images/sdk_button.png\",\n" +
            "  \"partnerToken\": \"F9E93D721E717B0F71B3\",\n" +
            "  \"privateKey\": \"afa9417b1ae37e18f32dd15844dc812dccdc30a5\"\n" +
            "}";
    String customerJSON = "{ \"members_users_email\": \"lborsato@goboomtown.com\" }";
    Context appContext = null;
    SupportSDK supportSDK = null;


    @Before
    public void init() throws Exception {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        supportSDK = new SupportSDK(appContext, configJSONString, null, null);
    }

    @Test
    public void testAppContext() throws Exception {
         assertThat(appContext.getPackageName(), startsWith("com.goboomtown.supportsdk."));
    }


}
