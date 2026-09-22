package us.shandian.giga.service;

import android.app.Service;

import us.shandian.giga.preprocessing.PvcHlsPreProcessor;
import us.shandian.giga.preprocessing.PvcPreprocessing;
import us.shandian.giga.get.DownloadMission;

public abstract class PvcDownloadManagerService extends Service {

    protected static final String EXTRA_SEGMENTS = "DownloadManagerService.extra.segments";

    protected void pvcLaunchHlsPreProcessor(
            final DownloadMission mission,
            final DownloadManager mManager) {
        final Thread prepareHlsDownload = new Thread(() -> { // own thread as we have network I/O
            final PvcPreprocessing preProcessor = new PvcHlsPreProcessor();
            preProcessor.modifyMission(mission);
            mManager.startMission(mission);
        });
        prepareHlsDownload.setName("PrepareHls");
        prepareHlsDownload.start();
    }

}
