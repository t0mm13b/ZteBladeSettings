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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ZteBladeStartup  extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean blnUSBCharge = prefs.getBoolean(context.getString(R.string.keyUSBCharging), true); 
        ZteBladeActivity._sProxSensorDataFile = context.getString(R.string.fileNameProxSensData);
        ZteBladeActivity._sUsbFileName = context.getString(R.string.fileNameUsb);
		String sMsg = "";
		if(blnUSBCharge) sMsg = String.format(context.getString(R.string.notifyMessageUSBStatus), context.getString(R.string.notifyMessageUSBEnabled)); 
		else sMsg = String.format(context.getString(R.string.notifyMessageUSBStatus), context.getString(R.string.notifyMessageUSBDisabled));
		ZteBladeActivity.WriteUSBValue(blnUSBCharge);
		ZteBladeActivity.notifyUSB(context, context.getString(R.string.app_name), sMsg);
	}
}
