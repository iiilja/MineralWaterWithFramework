// AidlInterface.aidl
package ee.promobox.promoboxandroid;

import ee.promobox.promoboxandroid.data.Campaign;
import ee.promobox.promoboxandroid.data.Display;
import ee.promobox.promoboxandroid.data.ErrorMessage;

// Declare any non-default types here with import statements

interface AidlInterface {

    void addError(in ErrorMessage errorMessage, boolean broadcastNow);
    void setActivityReceivedUpdate(boolean activityReceivedUpdate);
    // Replaces getCampaigns in mainActivity
    Campaign getCampaignWithId(int campaignId);
    boolean isVideoWall();
    int getWallWidth();
    int getWallHeight();
    String getAudioDevice();
    void setCurrentFileId(int id);
    int getCurrentFileId();
    void setUuid(String uuid);
    String getUuid();
    int getOrientation();
    Campaign getCurrentCampaign();
    void checkAndDownloadCampaign();
    List<Display> getDisplays();
    void setClosedNormally(boolean closedNormally);

}
