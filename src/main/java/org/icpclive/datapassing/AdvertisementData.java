package org.icpclive.datapassing;

import com.vaadin.data.util.BeanItemContainer;
import org.icpclive.webadmin.mainscreen.Advertisement;
import org.icpclive.webadmin.mainscreen.MainScreenData;

/**
 * Created by Aksenov239 on 21.11.2015.
 */
public class AdvertisementData extends CachedData {
    public AdvertisementData initialize() {
        AdvertisementData data = MainScreenData.getMainScreenData().advertisementData;
        synchronized (advertisementLock) {
            this.timestamp = data.timestamp;
            this.isVisible = data.isVisible;
            this.advertisement = data.advertisement == null ? new Advertisement("") : new Advertisement(data.advertisement.getAdvertisement());
            this.delay = data.delay;
        }
        return this;
    }

    public void recache() {
        Data.cache.refresh(AdvertisementData.class);
    }

    public void update() {
        boolean change = false;
        synchronized (advertisementLock) {
            if (System.currentTimeMillis() > timestamp + MainScreenData.getProperties().timeAdvertisement) {
                //System.err.println("Big idle for advert");
                isVisible = false;
                change = true;
            }
        }
        if (change) {
            recache();
        }
    }

    public String toString() {
        return isVisible ? "Advertisement \"" + advertisement.getAdvertisement() + "\"" : "No advertisement now";
    }

    public void setValue(Object key, String value) {
        MainScreenData.getProperties().backupAdvertisements.setProperty(key, "advertisement", value);
    }

    public BeanItemContainer<Advertisement> getContainer() {
        return MainScreenData.getProperties().backupAdvertisements.getContainer();
    }

    public void removeAdvertisement(Advertisement advertisement) {
        MainScreenData.getProperties().backupAdvertisements.removeItem(advertisement);
    }

    public void addAdvertisement(Advertisement advertisement) {
        MainScreenData.getProperties().backupAdvertisements.addItem(advertisement);
    }

    public String checkOverlays() {
//        if (MainScreenData.getMainScreenData().teamData.isVisible) {
//            return MainScreenData.getMainScreenData().teamData.getOverlayError();
//        }
        return null;
    }

    public void hide() {
        synchronized (advertisementLock) {
            timestamp = System.currentTimeMillis();
            isVisible = false;
        }
    }

    public String setAdvertisementVisible(boolean visible, Advertisement advertisement) {
        if (visible) {
            String outcome = checkOverlays();
            if (outcome != null) {
                return outcome;
            }
        }
        synchronized (advertisementLock) {
            timestamp = System.currentTimeMillis();
            isVisible = visible;
            this.advertisement = advertisement;
        }
        recache();
        return null;
    }

    public boolean isVisible;
    public Advertisement advertisement;

    final private Object advertisementLock = new Object();
}
