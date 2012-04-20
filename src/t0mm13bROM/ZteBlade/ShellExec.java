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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

public class ShellExec {
	private static final String TAG = "ShellExec";
	private static final boolean DEBUG = false;
	private IShellExec _iShellExec;
	public ShellExec(Context ctxt, IShellExec iSuperUserUtils){
		this._iShellExec = iSuperUserUtils;
	}
	public void RunCommand(int whatMessage, String sCommand, boolean blnWantOutCap){
		StringBuffer sbStdOut = new StringBuffer();
		if (DEBUG) Log.d(TAG, String.format("RunCommand(...) *** ENTER ***"));
		if (DEBUG) Log.d(TAG, String.format("sCommand = %s", sCommand));
		boolean rv = false;
		try {
			Process console = Runtime.getRuntime().exec(sCommand);
			if (console != null){
				if (blnWantOutCap){
					BufferedReader readerStdOut = new BufferedReader(new InputStreamReader(console.getInputStream()));
					int bytesRead = 0;
					char[] buffer = new char[4096];
					
					while ((bytesRead = readerStdOut.read(buffer)) > 0){
						sbStdOut.append(buffer, 0, bytesRead);
					}
					readerStdOut.close();
					sbStdOut.trimToSize();
				}
			}else{
				if (DEBUG) Log.d(TAG, String.format("RunCommand(...) console is null!?!"));
			}
			int nCmdResult = -1;
			try {
				nCmdResult = console.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (nCmdResult != 255) rv = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (this._iShellExec != null) this._iShellExec.cbShellExecd(whatMessage, rv, sbStdOut.toString().trim());
		if (DEBUG) Log.d(TAG, String.format("RunCommand(...) *** LEAVE ***"));
	
	}

}
