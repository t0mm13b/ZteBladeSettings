/* 
    Copyright (C) 2012, Tom Brennan (t0mm13b)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
package t0mm13bROM.ZteBlade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import t0mm13bROM.ZteBlade.R;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ZteBladeActivity extends PreferenceActivity implements IShellExec, OnSharedPreferenceChangeListener {
	public static final int ZteBlade_NOTIFY = 0xC0FFEE;
	private SharedPreferences _prefs = null;
	private CheckBoxPreference _chkBoxUSBCharging;
	private ShellExec _shExec;
	private ZteBladeActivityHandler _zbaHandlr;
	private boolean _blnCanCalibrate = true;
	private boolean _blnCanDisplay = true;
	private boolean _blnCanClear = true;
	private final int PROXSENSOR_CALIBRATE_RESULT = 0x100;
	private final int PROXSENSOR_DISPLAY_RESULT = 0x101;
	private final int PROXSENSOR_CLEAR_RESULT = 0x102;
	private final int USB_STATUS_GET_PREF = 0x103;
	//
	private static final String TAG = "ZteBladeActivity";
	private static final boolean DEBUG = false;
	//
	public static String _sProxSensorDataFile;
	public static String _sUsbFileName; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this._zbaHandlr = new ZteBladeActivityHandler();
        this._shExec = new ShellExec(getApplicationContext(), this);
        addPreferencesFromResource(R.xml.ztebladeprefs);
        PreferenceManager.setDefaultValues(this, R.xml.ztebladeprefs, false);
        //
        ZteBladeActivity._sProxSensorDataFile = getString(R.string.fileNameProxSensData);
        ZteBladeActivity._sUsbFileName = getString(R.string.fileNameUsb);
		if (DEBUG) Log.d(TAG, "onCreate(...): usbCharging Value = " + this._prefs.getBoolean(getString(R.string.keyUSBCharging), true));
        //
        final Runnable runCalibrateProxSens = new Runnable(){

			@Override
			public void run() {
				String sCmd = String.format("%s\n", getString(R.string.cmdProxSensorCalibrate));
				if (DEBUG) Log.d(TAG, "runCalibrateProxSens::run() sCmd = " + sCmd);
				_shExec.RunCommand(PROXSENSOR_CALIBRATE_RESULT, sCmd, true);
			}
        	
        };
        final Runnable runDisplayProxSens = new Runnable(){

			@Override
			public void run() {
				String sCmd = String.format("%s\n", getString(R.string.cmdProxSensorDisplay));
				if (DEBUG) Log.d(TAG, "runDisplayProxSens::run() sCmd = " + sCmd);
				_shExec.RunCommand(PROXSENSOR_DISPLAY_RESULT, sCmd, true);
			}
        	
        };
        final Runnable runClearProxSens = new Runnable(){
        	@Override
			public void run() {
        		String sCmd = String.format(getString(R.string.cmdRemove), _sProxSensorDataFile + "\n");
        		if (DEBUG) Log.d(TAG, "runClearProxSens::run() sCmd = " + sCmd);
        		_shExec.RunCommand(PROXSENSOR_CLEAR_RESULT, sCmd, false);
			}
        };
        //
        final Preference proxCalibrate = (Preference)findPreference(getString(R.string.keyProxSensorCalibrate));
        proxCalibrate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (_blnCanCalibrate){
					_blnCanCalibrate = false;
					Thread tRunCalibrate = new Thread(null, runCalibrateProxSens, "threadCalibrateProximitySensor");
					tRunCalibrate.start();
				}
				return true;
			}
		});
        final Preference proxDisplay = (Preference)findPreference(getString(R.string.keyProxSensorDisplay));
        proxDisplay.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (_blnCanDisplay){
					_blnCanDisplay = false;
					Thread tRunDisplay = new Thread(null, runDisplayProxSens, "threadDisplayProximitySensor");
					tRunDisplay.start();
				}
				return true;
			}
		});
        final Preference proxClear = (Preference)findPreference(getString(R.string.keyProxSensorClear));
        proxClear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (_blnCanClear){
					_blnCanClear = false;
					Thread tRunClear = new Thread(null, runClearProxSens, "threadClearProximitySensor");
					tRunClear.start();
				}
				
				return true;
			}
		});
        this._chkBoxUSBCharging = (CheckBoxPreference)findPreference(getString(R.string.keyUSBCharging));
        this._chkBoxUSBCharging.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				boolean blnUSBChargingFlag = Boolean.getBoolean(newValue.toString());
				Editor e = _prefs.edit();
				e.putBoolean(getString(R.string.keyUSBCharging), blnUSBChargingFlag);
				e.commit();
				return true;
			}
		});
        this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onStart(){
    	super.onStart();
		if (DEBUG) Log.d(TAG, "onStart(...): usbCharging Value = " + this._prefs.getBoolean(getString(R.string.keyUSBCharging), true));
    	this.getZteBladePrefs();
	}
    @Override
    public void onPause() {
    	super.onPause();
		if (DEBUG) Log.d(TAG, "onPause(...): usbCharging Value = " + this._prefs.getBoolean(getString(R.string.keyUSBCharging), true));
    	this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onResume(){
    	super.onResume();
		if (DEBUG) Log.d(TAG, "onResume(...): usbCharging Value = " + this._prefs.getBoolean(getString(R.string.keyUSBCharging), true));
    	this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (DEBUG) Log.d(TAG, "onSharedPreferenceChanged(...): usbCharging Value = " + this._prefs.getBoolean(getString(R.string.keyUSBCharging), true));
		if (DEBUG) Log.d(TAG, "onSharedPreferenceChanged(...): usbCharging Value! = " + sharedPreferences.getBoolean(getString(R.string.keyUSBCharging), true));
		String sMsg = "";
    	boolean blnUSBCharge = sharedPreferences.getBoolean(getString(R.string.keyUSBCharging), true);
    	if (blnUSBCharge) sMsg = String.format(getString(R.string.notifyMessageUSBStatus), getString(R.string.notifyMessageUSBEnabled));
    	else sMsg = String.format(getString(R.string.notifyMessageUSBStatus), getString(R.string.notifyMessageUSBDisabled));
		ZteBladeActivity.WriteUSBValue(blnUSBCharge);
    	ZteBladeActivity.notifyUSB(this, getString(R.string.app_name), sMsg);
	}
    private void getZteBladePrefs(){
        this._prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sCmd = String.format("%s\n", getString(R.string.cmdShowUSB));
    	if (DEBUG) Log.d(TAG, "getZteBladePrefs() sCmd = " + sCmd);
    	this._shExec.RunCommand(USB_STATUS_GET_PREF, sCmd, true);    	
    }
    @Override
	public void cbShellExecd(int whatMessage, boolean rvExecd, String sResults) {
		Message m = _zbaHandlr.obtainMessage();
		m.what = whatMessage;
		m.obj = (Object) sResults;
		_zbaHandlr.sendMessage(m);
	}
    public static void notifyUSB(Context context, String sTitle, String sMessage){
		NotificationManager nmNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (nmNotificationManager != null){
			int thisAppIcon = R.drawable.icon;
			long whenNotified = System.currentTimeMillis();
			Notification notification = new Notification(thisAppIcon, sMessage, whenNotified);
			Intent notifyUSBIntent = new Intent(context, ZteBladeActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifyUSBIntent, 0);
			notification.flags = (Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT);
			notification.setLatestEventInfo(context, sTitle, sMessage, contentIntent);
			nmNotificationManager.notify(ZteBlade_NOTIFY, notification);
		}
	}
    public static void WriteUSBValue(boolean blnVal){
    	File usbFile = new File(_sUsbFileName);
    	if (usbFile != null && usbFile.exists()){
    		FileOutputStream usbOut = null;
    		try{
    			usbOut = new FileOutputStream(new File(_sUsbFileName));
    		}catch(IOException ioOpenEx){
    			Log.w(TAG, "WriteUSBValue(" + blnVal + "): Got IO Exception when attempting to open " + _sUsbFileName + "; Details of IO Exception = " + ioOpenEx.toString());
    			usbOut = null;
    		}
        	if (usbOut != null){
        		try{
        			int nVal = (blnVal) ? 1 : 0;
        			usbOut.write(String.valueOf(nVal).getBytes());
        			usbOut.flush();
        			usbOut.getFD().sync();
        		}catch(IOException ioEx){
        			Log.w(TAG, "WriteUSBValue(" + blnVal + "): Got IO Exception when attempting to manipulate " + _sUsbFileName + "; Details of IO Exception = " + ioEx.toString());
        		}finally{
        			try{
        				usbOut.close();
        			}catch(IOException ioExClose){
        				Log.w(TAG, "WriteUSBValue(" + blnVal + "): Got IO Exception when attempting to close " + _sUsbFileName + "; Details of IO Exception = " + ioExClose.toString());
        			}
        		}
        	}else{
    			Log.w(TAG, "WriteUSBValue(" + blnVal + "): Could not open " + _sUsbFileName + "!");
        	}
    	}else{
			Log.w(TAG, "WriteUSBValue(" + blnVal + "): " + _sUsbFileName + " not found!");
    	}
    }
	private class ZteBladeActivityHandler extends Handler{
		@Override
		public void handleMessage(Message msg){
			String sResults = msg.obj.toString();
			switch(msg.what){
			case PROXSENSOR_CALIBRATE_RESULT :
				Toast.makeText(ZteBladeActivity.this, sResults, Toast.LENGTH_LONG).show();
				_blnCanCalibrate = true;
				break;
			case PROXSENSOR_DISPLAY_RESULT :
				ProxSensorDataDlg dlg = new ProxSensorDataDlg(ZteBladeActivity.this);
				dlg.setTitle("Proximity Sensor Data");
				dlg.setMessage(sResults);
				dlg.show();
				_blnCanDisplay = true;
				break;
			case PROXSENSOR_CLEAR_RESULT :
				Toast tClearProxSensor;
				File f = new File(_sProxSensorDataFile);
				if(f.exists()){
					// Should not get here!
					tClearProxSensor = Toast.makeText(ZteBladeActivity.this, getString(R.string.toastProxSensorDataError), Toast.LENGTH_LONG);
				}else{
					tClearProxSensor = Toast.makeText(ZteBladeActivity.this, getString(R.string.toastProxSensorDataSuccess), Toast.LENGTH_LONG);
				}
				tClearProxSensor.show();
				_blnCanClear = true;
				break;
			case USB_STATUS_GET_PREF :
				if (DEBUG) Log.d(TAG, "ZteBladeActivityHandler::handleMessage; sResults = " + sResults);
				boolean blnVal = (sResults.trim().compareTo("1") == 0) ? true : false; 
				if (DEBUG) Log.d(TAG, "ZteBladeActivityHandler::handleMessage; blnVal = " + blnVal);
				_chkBoxUSBCharging.setChecked(blnVal);
				break;
			}
		}
	}
	private class ProxSensorDataDlg extends Dialog{
		private TextView _tvTitle;
		private TextView _tvMessage;
		private Button _btnClose;
		public ProxSensorDataDlg(Context context) {
			super(context);
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.setContentView(R.layout.proxsensordata);
			this._tvTitle = (TextView)findViewById(R.id.tvTitle);
			this._tvMessage = (TextView)findViewById(R.id.tvMessage);
			this._btnClose = (Button)findViewById(R.id.btnClose);
			this._btnClose.setOnClickListener(new ProxSensorDataDlgClose());
		}
		public void setTitle(String sTitle){
			this._tvTitle.setText(sTitle);
		}
		public void setMessage(String sMessage){
			this._tvMessage.setText(sMessage);
		}
		private class ProxSensorDataDlgClose implements android.view.View.OnClickListener{

			@Override
			public void onClick(View v) {
				ProxSensorDataDlg.this.dismiss();
				
			}
			
		}
	}
	
}