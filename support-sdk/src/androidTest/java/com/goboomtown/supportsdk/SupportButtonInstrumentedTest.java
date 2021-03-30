package com.goboomtown.supportsdk;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.goboomtown.supportsdk.view.SupportButton;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SupportButtonInstrumentedTest {

    String configJSON = "{\n" +
            "  \"apiHost\": \"https://api.preprod.goboomtown.com\",\n" +
            "  \"integrationId\": \"ZEUNHK\",\n" +
            "  \"apiKey\": \"qWvbbg3knrhu62ZmTB5VlR9ysEHNdcDqJ2ra2uDBumnb\",\n" +
            "  \"buttonURL\":\"https://api.preprod.goboomtown.com/resources/images/sdk_button.png\",\n" +
            "  \"partnerToken\": \"F9E93D721E717B0F71B3\",\n" +
            "  \"privateKey\": \"afa9417b1ae37e18f32dd15844dc812dccdc30a5\"\n" +
            "}";
    String customerJSON = "{ \"members_users_email\": \"lborsato@goboomtown.com\" }";


    @Test
    public void init() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SupportButton supportButton = new SupportButton(appContext);
        assertNotNull(supportButton);
    }

    @Test
    public void loadConfiguration() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        SupportButton supportButton = new SupportButton(appContext);
        supportButton.loadConfiguration(configJSON, customerJSON);
        assertNotNull(supportButton);
    }

}
