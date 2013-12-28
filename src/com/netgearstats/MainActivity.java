package com.netgearstats;

import android.view.*;
import android.app.*;
import android.app.Dialog;
import android.widget.*;
import android.content.*;
import android.graphics.Color;
import android.net.*;
import android.os.*;
import android.widget.AdapterView.OnItemClickListener;
import java.util.*;
import java.io.*;
import java.text.*;
import java.lang.reflect.*;
import android.text.Html;
import org.jsoup.*;
import org.jsoup.nodes.*;
import android.util.*;
import android.content.pm.*;
import android.content.res.*;

public class MainActivity extends Activity {

    List<Info> Infos = new ArrayList<Info>();
    InfoAdapter adapter;
    ListView listView;
    SharedPreferences prefs = null;
    Integer refreshrate = 30000;
    List<Profile> Profiles = new ArrayList<Profile>();
    int CurrProfile = -1;
    TextView RouterModel;
    TextView RefreshCountdown;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getOverflowMenu();

        Infos.add(new Info(R.drawable.uptime, "Uptime", ""));
        Infos.add(new Info(R.drawable.wan, "WAN", ""));
        Infos.add(new Info(R.drawable.lan, "LAN", ""));
        Infos.add(new Info(R.drawable.wifi, "WLAN", ""));
        Infos.add(new Info(R.drawable.download, "Download", ""));
        Infos.add(new Info(R.drawable.upload, "Upload", ""));

        RouterModel = (TextView) findViewById(R.id.routermodel);
        RefreshCountdown = (TextView) findViewById(R.id.refreshcountdown);
        prefs = getSharedPreferences("com.netgearstats", MODE_PRIVATE);

        if (prefs.getString("refreshrate", null) != null) {
            refreshrate = Integer.parseInt(prefs.getString("refreshrate", null));
        } else {
            prefs.edit().putString("refreshrate", "30000").commit();
        }

        if (prefs.getBoolean("firstrun", true)) {
            CreateNewProfile();
            prefs.edit().putBoolean("firstrun", false).commit();
        } else {
            SelectProfile();
        }

    }

    Integer countdown = refreshrate / 1000;

    public void StartCountdown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isOnline()) {

                        runOnUiThread(new Runnable() {
                            public void run() {
                                RefreshCountdown.setText("Refresh in " + countdown + " sec");
                                countdown--;
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    ThrowException(e.getMessage());
                }
            }
        }).start();
    }

    public void StartUpdateLoop() {
        StartCountdown();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isOnline()) {
                        final Document Document = getDocument("http://" + Profiles.get(CurrProfile).IpAddress + "/RST_stattbl.htm");

                        String Uptime = Document.select("table").get(0).select("tr").get(1).select("td").get(0).text();
                        Uptime = Uptime.substring(Uptime.lastIndexOf(" ") + 1);

                        String LinkRateDownload = Document.select("table.tables").get(1).select("tr").get(1).select("td").get(1).text();
                        String LinkRateUpload = Document.select("table.tables").get(1).select("tr").get(1).select("td").get(2).text();

                        String DownloadAttenuation = Document.select("table.tables").get(1).select("tr").get(2).select("td").get(1).text();
                        String UploadAttenuation = Document.select("table.tables").get(1).select("tr").get(2).select("td").get(2).text();

                        String DownloadNoise = Document.select("table.tables").get(1).select("tr").get(3).select("td").get(1).text();
                        String UploadNoise = Document.select("table.tables").get(1).select("tr").get(3).select("td").get(2).text();

                        String WANStatus = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(1).text();
                        String WANTxPkts = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(2).text();
                        String WANRxPkts = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(3).text();
                        String WANCollisions = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(4).text();
                        String WANTxBs = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(5).text();
                        String WANRxBs = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(6).text();
                        String WANUptime = Document.select("table.tables").get(0).select("tr").get(1).select("td").get(7).text();

                        String LAN1 = "LAN 1 Status: " + Document.select("table.tables").get(0).select("tr").get(2).select("td").get(1).text() + "\n";
                        String LAN2 = "LAN 2 Status: " + Document.select("table.tables").get(0).select("tr").get(3).select("td").get(1).text() + "\n";
                        String LAN3 = "LAN 3 Status: " + Document.select("table.tables").get(0).select("tr").get(4).select("td").get(1).text() + "\n";
                        String LAN4 = "LAN 4 Status: " + Document.select("table.tables").get(0).select("tr").get(5).select("td").get(1).text() + "\n";
                        String LANTxPkts = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(2).text();
                        String LANRxPkts = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(3).text();
                        String LANCollisions = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(4).text();
                        String LANTxBs = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(5).text();
                        String LANRxBs = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(6).text();
                        String LANUptime = Document.select("table.tables").get(0).select("tr").get(2).select("td").get(7).text();

                        String WLANStatus = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(1).text();
                        String WLANTxPkts = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(2).text();
                        String WLANRxPkts = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(3).text();
                        String WLANCollisions = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(4).text();
                        String WLANTxBs = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(5).text();
                        String WLANRxBs = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(6).text();
                        String WLANUptime = Document.select("table.tables").get(0).select("tr").get(6).select("td").get(7).text();

                        Infos.get(0).Description = Uptime;
                        Infos.get(1).Description = "Status: " + WANStatus + "\nTxPkts: " + WANTxPkts + "\nRxPkts: " + WANRxPkts + "\nCollisions: " + WANCollisions + "\nTx B/s: " + WANTxBs + "\nRx B/s: " + WANRxBs + "\nUptime: " + WANUptime;
                        Infos.get(2).Description = LAN1 + LAN2 + LAN3 + LAN4 + "\nTxPkts: " + LANTxPkts + "\nRxPkts: " + LANRxPkts + "\nCollisions: " + LANCollisions + "\nTx B/s: " + LANTxBs + "\nRx B/s: " + LANRxBs + "\nUptime: " + LANUptime;
                        Infos.get(3).Description = "Status: " + WLANStatus + "\nTxPkts: " + WLANTxPkts + "\nRxPkts: " + WLANRxPkts + "\nCollisions: " + WLANCollisions + "\nTx B/s: " + WLANTxBs + "\nRx B/s: " + WLANRxBs + "\nUptime: " + WLANUptime;
                        Infos.get(4).Description = "Connection Speed: " + LinkRateDownload + "\nLine Attenuation: " + DownloadAttenuation + "\nNoise Margin: " + DownloadNoise;
                        Infos.get(5).Description = "Connection Speed: " + LinkRateUpload + "\nLine Attenuation: " + UploadAttenuation + "\nNoise Margin: " + UploadNoise;

                        runOnUiThread(new Runnable() {
                            public void run() {
                                BuildList(Infos);
                                if (RouterModel.getText() == "") {
                                    RouterModel.setText(Html.fromHtml("<b>" + Document.title() + "</b>"));
                                }
                            }
                        });

                        countdown = refreshrate / 1000;
                        Thread.sleep(refreshrate);
                    }
                } catch (Exception e) {
                    ThrowException(e.getMessage());
                }
            }
        }).start();
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public void ThrowException(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error");
                builder.setMessage(msg);
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setPositiveButton("Change profile", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SelectProfile();
                    }
                });
                builder.show();
            }
        });
    }

    private void FetchProfiles() {
        String profiles = prefs.getString("profiles", null);
        for (String profile : profiles.split("\\|\\|")) {
            String[] data = profile.split("\\|");
            String Name = data[0];
            String IpAddress = data[1];
            String Username = data[2];
            String Password = data[3];
            Profiles.add(new Profile(Name, IpAddress, Username, Password));
        }
    }

    private void SaveProfiles() {
        String profiles = "";
        for (Profile p : Profiles) {
            profiles += p.Name + "|" + p.IpAddress + "|" + p.Username + "|" + p.Password + "||";
        }

        prefs.edit().putString("profiles", profiles).commit();
    }
    int lastChecked = 0;

    private void SelectProfile() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Profiles.isEmpty()) {
                    FetchProfiles();
                }

                final String[] ProfilesName = new String[Profiles.size()];

                for (int i = 0; i < Profiles.size(); i++) {
                    ProfilesName[i] = Profiles.get(i).Name;
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final View dialog_layout = getLayoutInflater().inflate(R.layout.select_profile_dialog_layout, null);
                final ListView lv = (ListView) dialog_layout.findViewById(R.id.profiles);
                ArrayAdapter adapter1 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_single_choice, ProfilesName);
                lv.setAdapter(adapter1);
                lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                lv.setItemChecked(0, true);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    boolean somethingChecked = false;

                    public void onItemClick(AdapterView arg0, View arg1, int arg2,
                            long arg3) {
                        if (somethingChecked) {
                            ListView lv = (ListView) arg0;
                            TextView tv = (TextView) lv.getChildAt(lastChecked);
                            CheckedTextView cv = (CheckedTextView) tv;
                            cv.setChecked(false);
                        }
                        ListView lv = (ListView) arg0;
                        TextView tv = (TextView) lv.getChildAt(arg2);
                        CheckedTextView cv = (CheckedTextView) tv;
                        if (!cv.isChecked()) {
                            cv.setChecked(true);
                        }
                        lastChecked = arg2;
                        somethingChecked = true;
                    }
                });

                builder.setTitle("Profiles");
                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CurrProfile = lastChecked;

                        for (int i = 0; i < Infos.size(); i++) {
                            Infos.get(i).Description = "";
                            //if (Infos.get(i).ProgressBarProgress != -1) {
                            //    Infos.get(i).ProgressBarProgress = 0;
                            //}
                        }

                        StartUpdateLoop();
                    }
                });
                builder.setNeutralButton("New", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateNewProfile();
                    }
                });

                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                final AlertDialog Dialog = builder.create();

                Dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button deletebutton = Dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        deletebutton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (Profiles.size() == 1) {
                                    Toast.makeText(getApplicationContext(), "Can't delete the only profile available", Toast.LENGTH_SHORT).show();
                                } else {
                                    Profiles.remove(lastChecked);
                                    SaveProfiles();
                                    dialog.dismiss();
                                    SelectProfile();
                                }
                            }
                        });
                    }
                });

                Dialog.setView(dialog_layout);
                Dialog.show();
            }
        });
    }

    public void ShowChangeRefreshRateDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final View dialog_layout = getLayoutInflater().inflate(R.layout.refreshrate_dialog_layout, null);
                final NumberPicker np = (NumberPicker) dialog_layout.findViewById(R.id.numberPicker1);
                np.setMaxValue(60);
                np.setMinValue(5);
                np.setWrapSelectorWheel(false);

                np.setValue(Integer.parseInt(prefs.getString("refreshrate", null)) / 1000);

                builder.setTitle("Change refresh rate");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        prefs.edit().putString("refreshrate", Integer.toString(np.getValue() * 1000)).commit();
                        refreshrate = np.getValue() * 1000;
                        countdown = np.getValue();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog Dialog = builder.create();
                Dialog.setView(dialog_layout);
                Dialog.show();
            }
        });
    }

    public void CreateNewProfile() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final View dialog_layout = getLayoutInflater().inflate(R.layout.profile_dialog_layout, null);

                builder.setTitle("Create new profile");

                final EditText ProfileName = (EditText) dialog_layout.findViewById(R.id.profilename);
                final EditText IpAddress = (EditText) dialog_layout.findViewById(R.id.ipaddress);
                final EditText username = (EditText) dialog_layout.findViewById(R.id.username);
                final EditText password = (EditText) dialog_layout.findViewById(R.id.password);

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Profiles.add(new Profile(ProfileName.getText().toString(), IpAddress.getText().toString(), username.getText().toString(), password.getText().toString()));

                        SaveProfiles();

                        SelectProfile();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

                AlertDialog Dialog = builder.create();
                Dialog.setView(dialog_layout);
                Dialog.show();
            }
        });
    }

    private void getOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.showprofiles:
                SelectProfile();
                return true;

            case R.id.changerefreshrate:
                ShowChangeRefreshRateDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }

    public void BuildList(List<Info> data) {
        if (adapter == null) {
            adapter = new InfoAdapter(this,
                    R.layout.listview_item_row, data);

            listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        } else {
            adapter.Update(data);
        }
    }

    private Document getDocument(String url) throws Exception {
        String login = Profiles.get(CurrProfile).Username + ":" + Profiles.get(CurrProfile).Password;
        String base64login = Base64.encodeToString(login.getBytes(), RESULT_OK);

        Document document = Jsoup
                .connect(url)
                .header("Authorization", "Basic " + base64login)
                .get();

        return document;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
