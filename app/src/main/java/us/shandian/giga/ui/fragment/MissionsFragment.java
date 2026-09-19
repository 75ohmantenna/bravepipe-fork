package us.shandian.giga.ui.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.schabi.newpipe.R;
import org.schabi.newpipe.streams.io.NoFileManagerSafeGuard;
import org.schabi.newpipe.streams.io.StoredFileHelper;
import org.schabi.newpipe.util.ServiceBinding;

import java.io.IOException;

import us.shandian.giga.get.DownloadMission;
import us.shandian.giga.service.DownloadManager;
import us.shandian.giga.service.DownloadManagerService;
import us.shandian.giga.service.DownloadManagerService.DownloadManagerBinder;
import us.shandian.giga.ui.adapter.MissionAdapter;

public class MissionsFragment extends Fragment {

    private static final String TAG = "MissionsFragment";
    private static final int SPAN_SIZE = 2;

    private SharedPreferences mPrefs;
    private boolean mLinear;
    private MenuItem mSwitch;
    private MenuItem mClear = null;
    private MenuItem mStart = null;
    private MenuItem mPause = null;

    private RecyclerView mList;
    private View mEmpty;
    private MissionAdapter mAdapter;
    private GridLayoutManager mGridManager;
    private LinearLayoutManager mLinearManager;
    private DownloadManagerBinder mBinder;
    private ServiceBinding mServiceBinding;
    private boolean mListening;
    private boolean mForceUpdate;

    private DownloadMission unsafeMissionTarget = null;
    private final ActivityResultLauncher<Intent> requestDownloadSaveAsLauncher =
            registerForActivityResult(new StartActivityForResult(), this::requestDownloadSaveAsResult);
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (mServiceBinding == null) {
                return;
            }
            mBinder = (DownloadManagerBinder) binder;
            mBinder.clearDownloadNotifications();

            mAdapter = new MissionAdapter(requireContext(), mBinder.getDownloadManager(),
                    mEmpty, requireView());

            mAdapter.setRecover(MissionsFragment.this::recoverMission);

            setMenuAvailable(true);
            setAdapterButtons();
            updateList();

            if (isResumed()) {
                resumeAdapter();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            releaseAdapter();
        }


    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.missions, container, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        mLinear = mPrefs.getBoolean("linear", false);

        // Views
        mEmpty = v.findViewById(R.id.list_empty_view);
        mList = v.findViewById(R.id.mission_recycler);

        // Init layouts managers
        mGridManager = new GridLayoutManager(getActivity(), SPAN_SIZE);
        mGridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mAdapter.getItemViewType(position)) {
                    case DownloadManager.SPECIAL_PENDING:
                    case DownloadManager.SPECIAL_FINISHED:
                        return SPAN_SIZE;
                    default:
                        return 1;
                }
            }
        });
        mLinearManager = new LinearLayoutManager(getActivity());

        mServiceBinding = new ServiceBinding(requireContext(),
                new Intent(requireContext(), DownloadManagerService.class), mConnection);
        mServiceBinding.bind(Context.BIND_AUTO_CREATE);

        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onDestroyView() {
        releaseAdapter();
        if (mServiceBinding != null) {
            mServiceBinding.unbind();
            mServiceBinding = null;
        }
        mList = null;
        mEmpty = null;
        mSwitch = null;
        mClear = null;
        mStart = null;
        mPause = null;
        super.onDestroyView();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mSwitch = menu.findItem(R.id.switch_mode);
        mClear = menu.findItem(R.id.clear_list);
        mStart = menu.findItem(R.id.start_downloads);
        mPause = menu.findItem(R.id.pause_downloads);

        setMenuAvailable(mAdapter != null);
        if (mAdapter != null) {
            setAdapterButtons();
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.switch_mode) {
            mLinear = !mLinear;
            updateList();
            return true;
        } else if (itemId == R.id.clear_list) {
            showClearDownloadHistoryPrompt();
            return true;
        } else if (itemId == R.id.start_downloads) {
            mBinder.getDownloadManager().startAllMissions();
            return true;
        } else if (itemId == R.id.pause_downloads) {
            mBinder.getDownloadManager().pauseAllMissions(false);
            mAdapter.refreshMissionItems();// update items view

            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showClearDownloadHistoryPrompt() {
        // ask the user whether he wants to just clear history or instead delete files on disk
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.clear_download_history)
                .setMessage(R.string.confirm_prompt)
                // Intentionally misusing buttons' purpose in order to achieve good order
                .setNegativeButton(R.string.clear_download_history, (dialog, which) ->
                        mAdapter.clearFinishedDownloads(false))
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_downloaded_files, (dialog, which) ->
                        showDeleteDownloadedFilesConfirmationPrompt())
                .show();
    }

    public void showDeleteDownloadedFilesConfirmationPrompt() {
        // make sure the user confirms once more before deleting files on disk
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_downloaded_files_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        mAdapter.clearFinishedDownloads(true))
                .show();
    }

    private void updateList() {
        if (mLinear) {
            mList.setLayoutManager(mLinearManager);
        } else {
            mList.setLayoutManager(mGridManager);
        }

        // destroy all created views in the recycler
        mList.setAdapter(null);
        mAdapter.notifyDataSetChanged();

        // re-attach the adapter in grid/lineal mode
        mAdapter.setLinear(mLinear);
        mList.setAdapter(mAdapter);

        if (mSwitch != null) {
            mSwitch.setIcon(mLinear
                            ? R.drawable.ic_apps
                            : R.drawable.ic_list);
            mSwitch.setTitle(mLinear ? R.string.grid : R.string.list);
            mPrefs.edit().putBoolean("linear", mLinear).apply();
        }
    }

    private void setAdapterButtons() {
        if (mClear == null || mStart == null || mPause == null) {
            return;
        }

        mAdapter.setClearButton(mClear);
        mAdapter.setMasterButtons(mStart, mPause);
    }

    private void setMenuAvailable(final boolean available) {
        if (mSwitch != null) {
            mSwitch.setEnabled(available);
        }
        if (mClear != null) {
            mClear.setEnabled(available);
        }
        if (mStart != null) {
            mStart.setEnabled(available);
        }
        if (mPause != null) {
            mPause.setEnabled(available);
        }
    }

    private void recoverMission(@NonNull DownloadMission mission) {
        unsafeMissionTarget = mission;

        NoFileManagerSafeGuard.launchSafe(
                requestDownloadSaveAsLauncher,
                StoredFileHelper.getNewPicker(requireContext(), mission.storage.getName(),
                        mission.storage.getType(), null),
                TAG,
                requireContext()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();

        pauseAdapter();
    }

    private void requestDownloadSaveAsResult(final ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (unsafeMissionTarget == null || result.getData() == null || mAdapter == null) {
            return;
        }

        try {
            final Uri fileUri = result.getData().getData();
            final String tag = unsafeMissionTarget.storage.getTag();
            unsafeMissionTarget.storage = new StoredFileHelper(requireContext(), null,
                    fileUri, tag);
            mAdapter.recoverMission(unsafeMissionTarget);
        } catch (final IOException e) {
            Toast.makeText(requireContext(), R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    private void resumeAdapter() {
        if (mAdapter == null || mBinder == null) {
            return;
        }
        mAdapter.onResume();
        if (mForceUpdate) {
            mForceUpdate = false;
            mAdapter.forceUpdate();
        }
        if (!mListening) {
            mBinder.addMissionEventListener(mAdapter);
            mListening = true;
        }
        mBinder.enableNotifications(false);
        mAdapter.checkMasterButtonsVisibility();
    }

    private void pauseAdapter() {
        if (mAdapter == null || mBinder == null) {
            return;
        }
        mForceUpdate = true;
        if (mListening) {
            mBinder.removeMissionEventListener(mAdapter);
            mListening = false;
        }
        mAdapter.onPaused();
        mBinder.enableNotifications(true);
    }

    private void releaseAdapter() {
        setMenuAvailable(false);
        if (mAdapter != null) {
            if (mBinder != null && mListening) {
                mBinder.removeMissionEventListener(mAdapter);
            }
            mAdapter.onDestroy();
        }
        if (mBinder != null) {
            mBinder.enableNotifications(true);
        }
        mListening = false;
        mBinder = null;
        mAdapter = null;
    }
}
