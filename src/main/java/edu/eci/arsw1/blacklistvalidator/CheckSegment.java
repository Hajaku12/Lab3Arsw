package edu.eci.arsw1.blacklistvalidator;

import edu.eci.arsw1.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class CheckSegment extends Thread {

    private HostBlacklistsDataSourceFacade dataSource;
    private int startIndex;
    private int endIndex;
    private String ipaddress;
    private SharedState sharedState;

    public CheckSegment(HostBlacklistsDataSourceFacade dataSource, int startIndex, int endIndex, String ipaddress, SharedState sharedState) {
        this.dataSource = dataSource;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.ipaddress = ipaddress;
        this.sharedState = sharedState;
    }

    @Override
    public void run() {
        for (int i = startIndex; i < endIndex && !sharedState.shouldStop(); i++) {
            if (dataSource.isInBlackListServer(i, ipaddress)) {
                sharedState.addOccurrence(i);
                if (sharedState.getTotalOccurrences() >= HostBlackListsValidator.BLACK_LIST_ALARM_COUNT) {
                    sharedState.requestStop();
                    break;
                }
            }
        }
    }
}