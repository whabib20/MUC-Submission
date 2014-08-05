package com.football.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.football.app.R;
import com.football.app.util.Connectivity;

public class Splash extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splash);

		checkConnectivity();
	}

	private void checkConnectivity() {
		if (!Connectivity.isConnected(this)) {
			showInfoMessageDialog("No Connection",
					"Please turn on WIFI and try again!");
		} else {
			final Intent intent = new Intent(this, BrazilWCMapActivity.class);// (this,BrazilWCMapActivity.class);
			Thread logoTimer = new Thread() {
				public void run() {
					try {
						int logoTimer = 0;
						while (logoTimer < 5000) {
							sleep(100);
							logoTimer = logoTimer + 100;
						}
						;

						startActivity(intent);// Constants.MAIN_ACTIVITY));
					}

					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					finally {
						finish();
					}
				}
			};

			logoTimer.start();

		}
	}

	private void showInfoMessageDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title)
				.setMessage(message)
				.setCancelable(false)
				.setNegativeButton("Try Again",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								checkConnectivity();
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}

}