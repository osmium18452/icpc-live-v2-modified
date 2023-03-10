package org.icpclive.datapassing;

import org.icpclive.webadmin.mainscreen.MainScreenData;

public class StatisticsData extends CachedData {

    public void recache() {
        Data.cache.refresh(StatisticsData.class);
    }

    public synchronized String setVisible(boolean visible) {
        delay = 0;
        if (visible) {
            String outcome = checkOverlays();
            if (outcome != null) {
                return outcome;
            }
            switchOverlaysOff();
        }
        timestamp = System.currentTimeMillis();
        isVisible = visible;
        recache();
        return null;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public StatisticsData initialize() {
        StatisticsData data = MainScreenData.getMainScreenData().statisticsData;
        this.timestamp = data.timestamp;
        this.isVisible = data.isVisible;
        this.delay = data.delay;

        return this;
    }

    public String checkOverlays() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        if (mainScreenData.teamData.isVisible) {
            return mainScreenData.teamData.getOverlayError();
        }
        if (mainScreenData.pvpData.isVisible()) {
            return mainScreenData.pvpData.getOverlayError();
        }
        return null;
    }

    public void switchOverlaysOff() {
        MainScreenData mainScreenData = MainScreenData.getMainScreenData();
        boolean turnOff = false;
        if (mainScreenData.standingsData.isVisible &&
                mainScreenData.standingsData.isBig) {
            mainScreenData.standingsData.hide();
            turnOff = true;
        }
        if (mainScreenData.pollData.isVisible) {
            mainScreenData.pollData.hide();
            turnOff = true;
        }
        if (mainScreenData.pictureData.isVisible()) {
            mainScreenData.pictureData.hide();
            turnOff = true;
        }
        if (turnOff) {
            delay = MainScreenData.getProperties().overlayedDelay;
        } else {
            delay = 0;
        }
    }

    @Override
    public void hide() {
        delay = 0;
        setVisible(false);
    }

    private boolean isVisible;
}
