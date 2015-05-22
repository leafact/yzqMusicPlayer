package com.yzqmusicplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.example.yzqmusicplayer.R;

public class LoadingActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
				overridePendingTransition(0, R.anim.out_from_top);
			}
		}, 2000);
	}

}
