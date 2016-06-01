package org.roysin.wechathelper;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends  Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fgMgr = getFragmentManager();
        FragmentTransaction transaction = fgMgr.beginTransaction();
        SettingsPreference settingsFrag = new SettingsPreference();
        transaction.add(R.id.main_container,settingsFrag);
        transaction.commit();

    }

}
