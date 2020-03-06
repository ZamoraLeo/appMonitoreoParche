package com.zamoraleo.appMonitoreoParche;

import com.google.firebase.database.FirebaseDatabase;

public class Firebase extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
