package com.vinhteam.sendsms;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    EditText txtSoDT, txtNoiDung;
    TextView txtLog;
    Button cmdSend, cmdGetContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Click vô gởi để bắt đầu!", Snackbar.LENGTH_LONG)
                        .setAction("Gởi", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendAll();
                            }
                        }).show();
            }
        });

        txtSoDT = findViewById(R.id.txtSoDT);
        txtNoiDung = findViewById(R.id.txtNoiDung);
        txtLog = findViewById(R.id.txtLog);
        cmdGetContacts = findViewById(R.id.cmdGetContacts);

        txtSoDT.setText("098xxxxxx Nospam");
        String msg = "Năm mới chúc mừng anh %s vạn sự như ý.";
        txtNoiDung.setText(msg);

//        cmdSend = findViewById(R.id.cmdSend);

//        cmdSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
////                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
////                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
////                        .setNegativeButton("No", dialogClickListener).show();
//
//                final Dialog dialog = new Dialog(MainActivity.this, R.style.NewDialog);
//                dialog.setCanceledOnTouchOutside(true);
//                dialog.setContentView(R.layout.layout_message_dlg);
//                dialog.getWindow().setLayout(300, 300);
//                dialog.show();
//
//                final Timer t = new Timer();
//                t.schedule(new TimerTask() {
//                    public void run() {
//                        dialog.dismiss(); // when the task active then close the dialog
//                        t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
//                    }
//                }, 2000); // after 2 second (or 2000 miliseconds), the task will be active.
//
//            }
//        });

        cmdGetContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(intent, PICK_CONTACT);

                Intent intent = new Intent(MainActivity.this, ContactPickerMulti.class);
                startActivityForResult(intent, PICK_CONTACT_MULTIPLE);
            }
        });
    }

    final int PICK_CONTACT = 1, PICK_CONTACT_MULTIPLE = 2;

    @Override public void onActivityResult(int reqCode, int resultCode, Intent data){ super.onActivityResult(reqCode, resultCode, data);
        {
            switch (reqCode) {
                case (PICK_CONTACT):
                    if (resultCode == Activity.RESULT_OK) {
                        Uri contactData = data.getData();
                        Cursor c = managedQuery(contactData, null, null, null, null);
                        if (c.moveToFirst()) {
                            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                            String hasPhone =
                                    c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                            if (hasPhone.equalsIgnoreCase("1")) {
                                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                                phones.moveToFirst();
                                String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();
                                //setCn(cNumber);
                            }
                        }
                        ;
                    }
                    break;
                case PICK_CONTACT_MULTIPLE:
                    if (resultCode == RESULT_OK) {
                        if (data != null) {

                            String[] arrPhones = data.getStringArrayExtra("PICK_CONTACT");
                            StringBuilder sb = new StringBuilder();
                            for(String phone: arrPhones){
                                sb.append(phone +"\n");
                            }
                            txtSoDT.setText(sb.toString());
                        }
                    }
                    break;
            }

        }

    }


    private void sendSMS(String phoneNumber, String message)
    {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }



    void sendAll(){

        String phoneNumber, text;

        text = txtNoiDung.getText().toString();
        String txtPhones = txtSoDT.getText().toString();
        String msg, ten;
        String[] arrayString = txtPhones.split("\n");
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

//        SubscriptionManager subscriptionManager = SubscriptionManager.from(this.getApplicationContext());
//        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
//            int subscriptionId = subscriptionInfo.getSubscriptionId();
//
//        }

        for(int i = 0; i < arrayString.length; i++    ) {
            if(arrayString[i].trim().length()!=0) {

                String temp = arrayString[i];

                String part1 = arrayString[i].substring(0, temp.indexOf(' '));
                String part2 = temp.substring(temp.indexOf(' ') + 1);

                if(part1.equals("")){
                    phoneNumber = temp;
                    ten ="";
                }else
                {
                    phoneNumber = part1;
                    ten = part2;

                }

                msg = String.format(text, ten);
                phoneNumber = phoneNumber.trim();

                currentDateandTime = sdf.format(new Date());
                sb.append(String.format("Date: %s, sent [%s] %s", currentDateandTime, phoneNumber, ten));
                try {
                    //sendLongSMS(phoneNumber, msg);
                    sendSMS(phoneNumber, msg);
                    sb.append(String.format("..done\n"));
                }catch (Exception ex){
                    sb.append(String.format("..err %s\n", ex.getMessage()));
                }

            }
        }
        txtLog.setText(sb.toString());

    }
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    sendAll();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };
    public void sendLongSMS(String phoneNumber, String text) {
        String simID = "isms0";//isms0:sim_1,   isms1:sim_2
        SmsManager smsManager = SmsManager.getDefault();
//        smsManager.sendTextMessage(phoneNumber, null, text, null, null);

        ArrayList<String> parts = smsManager.divideMessage(text);
        smsManager.sendMultipartTextMessage(phoneNumber, simID, parts, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public static List<SimInfo> getSIMInfo(Context context) {
        List<SimInfo> simInfoList = new ArrayList<>();
        Uri URI_TELEPHONY = Uri.parse("content://telephony/siminfo/");
        Cursor c = context.getContentResolver().query(URI_TELEPHONY, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex("_id"));
                int slot = c.getInt(c.getColumnIndex("slot"));
                String display_name = c.getString(c.getColumnIndex("display_name"));
                String icc_id = c.getString(c.getColumnIndex("icc_id"));
                SimInfo simInfo = new SimInfo(id, display_name, icc_id, slot);
                //Log.d("apipas_sim_info", simInfo.toString());
                simInfoList.add(simInfo);
            } while (c.moveToNext());
        }
        c.close();

        return simInfoList;
    }
}
