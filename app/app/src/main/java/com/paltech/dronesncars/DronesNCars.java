package com.paltech.dronesncars;

import android.app.Application;

import com.paltech.dronesncars.model.Repository;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.components.SingletonComponent;

@HiltAndroidApp
@Module
@InstallIn(SingletonComponent.class)
public class DronesNCars extends Application {
    ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Provides
    public Executor getExecutor(){
        return executorService;
    }
}
