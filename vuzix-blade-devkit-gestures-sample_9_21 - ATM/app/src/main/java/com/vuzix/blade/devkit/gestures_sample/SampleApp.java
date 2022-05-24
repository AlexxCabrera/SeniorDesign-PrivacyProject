package com.vuzix.blade.devkit.gestures_sample;

import com.vuzix.hud.resources.DynamicThemeApplication;

public class SampleApp extends DynamicThemeApplication {
    @Override
    protected int getNormalThemeResId(){return R.style.AppTheme;}

    @Override
    protected int getLightThemeResId(){return R.style.AppTheme_Light;}
}
