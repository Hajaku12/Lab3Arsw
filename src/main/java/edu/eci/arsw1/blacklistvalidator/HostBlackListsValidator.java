package edu.eci.arsw1.blacklistvalidator;

import edu.eci.arsw1.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostBlackListsValidator {

    public static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    private HostBlacklistsDataSourceFacade dataSource;

    public HostBlackListsValidator() {
        this.dataSource = HostBlacklistsDataSourceFacade.getInstance();
    }

    public List<Integer> checkHost(String ipaddress, int numThreads) {
        SharedState sharedState = new SharedState();
        CheckSegment[] threads = threadGenerator(numThreads, ipaddress, sharedState);

        for (CheckSegment thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Thread interrupted", e);
            }
        }

        if (sharedState.getTotalOccurrences() >= BLACK_LIST_ALARM_COUNT) {
            LOG.log(Level.INFO, "The host was found in {0} blacklists", sharedState.getTotalOccurrences());
        } else {
            LOG.log(Level.INFO, "The host was found in less than {0} blacklists", BLACK_LIST_ALARM_COUNT);
        }

        return sharedState.getBlacklists();
    }

    private CheckSegment[] threadGenerator(int numThreads, String ipaddress, SharedState sharedState) {
        int serverCount = dataSource.getRegisteredServersCount();
        int segmentSize = serverCount / numThreads;
        CheckSegment[] threads = new CheckSegment[numThreads];

        for (int i = 0; i < numThreads; i++) {
            int startIndex = i * segmentSize;
            int endIndex = (i == numThreads - 1) ? serverCount : startIndex + segmentSize;
            threads[i] = new CheckSegment(dataSource, startIndex, endIndex, ipaddress, sharedState);
            threads[i].start();
        }

        return threads;
    }
}