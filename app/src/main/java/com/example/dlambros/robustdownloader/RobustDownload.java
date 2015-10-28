package com.example.dlambros.robustdownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;

public class RobustDownload extends Activity
{

    // Strings for logging
    private final String TAG = this.getClass().getSimpleName();
    private final String RESTORE = ", can restore state";

    // Checkboxes
    private CheckBox mWifi;
    private CheckBox mRUWifi;

    String url;

    // Download restrictions
    private boolean onlywifi = false;
    private boolean onlyRU = false;

    // Buttons
    private Button mDownload;
    private Button mLog;

    // Create a progress bar object to reference progress bar
    private ProgressBar mProgress;

    ProgressDialog _busyDialog = null;

    private class downloadRUWifiTask extends AsyncTask<String, Void, Void>
    {
        private long DownloadRef;

        @Override
        protected void onPostExecute(Void unused)
        {
            //mProgress.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute()
        {
           // mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            // Create a new instance of the Download Manager
            final DownloadManager downMan = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            // Create a URI from the URL provided
            Uri uri = Uri.parse(url);

            // Put request in download queue
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // Set the Download to be visible in the Downloads Interface
            request.setVisibleInDownloadsUi(true);

            // Set the Download to be visible in the Notifications after completion
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            while (((info.getSSID().toString().equals("\"LAWN\"")) |
                    (info.getSSID().toString().equals("\"RUWireless\"")) |
                    (info.getSSID().toString().equals("\"RUWireless_Secure\"")) ? true : false))
            {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Have download only on WIFI network
            request.setAllowedNetworkTypes(2*((info.getSSID().toString().equals("\"LAWN\"")) |
                                              (info.getSSID().toString().equals("\"RUWireless\"")) |
                                              (info.getSSID().toString().equals("\"RUWireless_Secure\"")) ? 1 : 0));

            System.out.println(info.getSSID().toString());
            DownloadRef = downMan.enqueue(request);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    int status = 0;

                    while (status != DownloadManager.STATUS_SUCCESSFUL)
                    {
                        // Create a query for the DownloadManager
                        DownloadManager.Query query = new DownloadManager.Query();

                        // Query only the specified reference ID
                        query.setFilterById(DownloadRef);

                        Cursor cursor = downMan.query(query);

                        cursor.moveToFirst();

                        int bytes_download = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        final double dl_progress = (double) ((bytes_download * 100) / bytes_total);
                        status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        RobustDownload.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProgress.setProgress((int) dl_progress);
                            }
                        });
                        cursor.close();
                    }
                }
            }).start();
            return null;
        }
    }

    private class downloadWifiTask extends AsyncTask<String, Void, Void>
    {
        private long DownloadRef;

        @Override
        protected void onPreExecute()
        {
           // mProgress.setVisibility(View.VISIBLE);
           // mProgress.setProgress(0);
        }

        @Override
        protected void onPostExecute(Void unused)
        {
            //mProgress.setProgress(100);
           // mProgress.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(String... params)
        {
            // Create a new instance of the Download Manager
            final DownloadManager downMan = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            // Create a URI from the URL provided
            Uri uri = Uri.parse(url);

            // Put request in download queue
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // Set the Download to be visible in the Downloads Interface
            request.setVisibleInDownloadsUi(true);

            // Set the Download to be visible in the Notifications after completion
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Have download only on WIFI network
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

            DownloadRef = downMan.enqueue(request);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    int status = 0;

                    while (status != DownloadManager.STATUS_SUCCESSFUL)
                    {
                        // Create a query for the DownloadManager
                        DownloadManager.Query query = new DownloadManager.Query();

                        // Query only the specified reference ID
                        query.setFilterById(DownloadRef);

                        Cursor cursor = downMan.query(query);

                        cursor.moveToFirst();

                        int bytes_download = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        final double dl_progress = (double) ((bytes_download * 100) / bytes_total);

                        status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                        RobustDownload.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProgress.setProgress((int) dl_progress);
                            }
                        });
                        cursor.close();
                    }
                }
            }).start();
             return null;
        }
    }

    private class downloadTask extends AsyncTask<String, Void, Void>
    {
        private long DownloadRef;

        @Override
        protected void onPreExecute()
        {
            //mProgress.setVisibility(View.VISIBLE);
            //mProgress.setProgress(0);
        }

        @Override
        protected Void doInBackground(String... params) {
            // Create a new instance of the Download Manager
            final DownloadManager downMan = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            // Create a URI from the URL provided
            Uri uri = Uri.parse(url);

            // Put request in download queue
            DownloadManager.Request request = new DownloadManager.Request(uri);

            // Set the Download to be visible in the Downloads Interface
            request.setVisibleInDownloadsUi(true);

            // Set the Download to be visible in the Notifications after completion
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            request.allowScanningByMediaScanner();

            // Have download only on WIFI network
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);


            // Send request into the download queue
            DownloadRef = downMan.enqueue(request);
            new Thread(new Runnable() {
                @Override
                public void run() {

                    int status = 0;

                    while (status != DownloadManager.STATUS_SUCCESSFUL)
                    {
                        // Create a query for the DownloadManager
                        DownloadManager.Query query = new DownloadManager.Query();

                        // Query only the specified reference ID
                        query.setFilterById(DownloadRef);

                        Cursor cursor = downMan.query(query);

                        cursor.moveToFirst();

                        int bytes_download = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        final double dl_progress = (double) ((bytes_download * 100) / bytes_total);

                        status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                        RobustDownload.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mProgress.setProgress((int) dl_progress);
                            }
                        });
                        cursor.close();
                    }
                }
            }).start();
            return null;
        }


        @Override
        protected void onPostExecute(Void unused)
        {
           // mProgress.setProgress(100);
            //mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robust_download);
        mProgress = (ProgressBar) findViewById(R.id.Progress);
        //mProgress.setVisibility(View.GONE);
        // When the WiFi check box is set, tell the app it can only download through WiFi
        mWifi = (CheckBox)findViewById(R.id.wifiBox);
        mWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    onlywifi = true;
                }
                else
                {
                    onlywifi = false;
                }
            }
        });

        // When the RUWifi check box is set, tell the app it can only download through Wifi
        mRUWifi = (CheckBox) findViewById(R.id.RUBox);
        mRUWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    onlyRU = true;
                }
                else
                {
                    onlyRU = false;
                }
            }
        });

        mDownload = (Button) findViewById(R.id.Download);
        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get text from link field
                EditText text = (EditText)findViewById(R.id.editText);
                url = text.getText().toString();
                if (onlyRU)
                {
                    new downloadRUWifiTask().execute();
                }
                else if (onlywifi)
                {
                    new downloadWifiTask().execute();
                }
               else
               {
                    new downloadTask().execute();
               }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_robust_download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showBusyDialog()
    {
        _busyDialog = ProgressDialog.show(this, "", "Downloading Link...", true);
    }

    public void dismissBusyDialog()
    {
        if (_busyDialog != null)
        {
            _busyDialog.dismiss();
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        // Notification that the activity will be started
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Notification that the activity is starting
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Notification that the activity will stop interacting with the user
        Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // Notification that the activity is no longer visible
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Notification that the activity will be destroyed
        Log.i(TAG,
                "onDestroy " // Log which, if any, configuration changed
                + Integer.toString(getChangingConfigurations(), 16));
    }

    ///////////////////////////////////////////////////////////////////////////////
    // Called during the lifecycle, when instance state should be saved/restored //
    ///////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // Save instance-specific state
        outState.putString("answer", "state to reload");
        super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);

        // Restore state
        String answer = null != savedState ? savedState.getString("answer") : "";

        Object oldTaskObject = getLastNonConfigurationInstance();

        if (null != oldTaskObject)
        {
            int oldtask = ((Integer) oldTaskObject).intValue();

            int currentTask = getTaskId();

            // Task should not change across a configuration change
            assert oldtask == currentTask;
        }
    }
}
