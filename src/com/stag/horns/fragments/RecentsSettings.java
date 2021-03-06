/*
 * Copyright (C) 2018 StagOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stag.horns.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.os.UserHandle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.stag.horns.R;
import com.android.internal.util.stag.StagUtils;

public class RecentsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_STOCK = "stock_recents";
    private static final String KEY_CATEGORY_IMMERSIVE = "immersive";
    private static final String KEY_CATEGORY_HAFR = "hafr";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
    private static final String RECENTS_LAYOUT_STYLE_PREF = "recents_layout_style";
    private static final String IMMERSIVE_RECENTS = "immersive_recents";
    private static final String RECENTS_DATE = "recents_full_screen_date";
    private static final String RECENTS_CLOCK = "recents_full_screen_clock";

    private PreferenceCategory mStockCat;
    private PreferenceCategory mImmersiveCat;
    private PreferenceCategory mHafrCat;
    private ListPreference mRecentsClearAllLocation;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsLayoutStylePref;
    private ListPreference mImmersiveRecents;
    private SwitchPreference mClock;
    private SwitchPreference mDate;

    private SharedPreferences mPreferences;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.horns_recents);
        ContentResolver resolver = getActivity().getContentResolver();
        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        // clear all recents
        mRecentsClearAllLocation = (ListPreference) findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        // recents layout style
        mRecentsLayoutStylePref = (ListPreference) findPreference(RECENTS_LAYOUT_STYLE_PREF);
        int type = Settings.System.getInt(resolver,
                Settings.System.RECENTS_LAYOUT_STYLE, 0);
        mRecentsLayoutStylePref.setValue(String.valueOf(type));
        mRecentsLayoutStylePref.setSummary(mRecentsLayoutStylePref.getEntry());
        mRecentsLayoutStylePref.setOnPreferenceChangeListener(this);

        // immersive recents
        mImmersiveRecents = (ListPreference) findPreference(IMMERSIVE_RECENTS);
        int mode = Settings.System.getInt(getContentResolver(),
        Settings.System.IMMERSIVE_RECENTS, 0);
            mImmersiveRecents.setValue(String.valueOf(mode));
        mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
        mImmersiveRecents.setOnPreferenceChangeListener(this);

        mStockCat = (PreferenceCategory) findPreference(KEY_CATEGORY_STOCK);
        mImmersiveCat = (PreferenceCategory) findPreference(KEY_CATEGORY_IMMERSIVE);
        mHafrCat = (PreferenceCategory) findPreference(KEY_CATEGORY_HAFR);
        updateRecentsState(type);

        mClock = (SwitchPreference) findPreference(RECENTS_CLOCK);
        mDate = (SwitchPreference) findPreference(RECENTS_DATE);
        updateDisablestate(mode);
    }



    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HORNS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void openAOSPFirstTimeWarning() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.aosp_first_time_title))
                .setMessage(getResources().getString(R.string.aosp_first_time_message))
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                }).show();
    }

    public void updateRecentsState(int type) {
        if (type == 0) {
           mStockCat.setEnabled(false);
           mImmersiveCat.setEnabled(false);
           mHafrCat.setEnabled(false);
        } else {
           mStockCat.setEnabled(true);
           mImmersiveCat.setEnabled(true);
           mHafrCat.setEnabled(true);
        }
    }

    public void updateDisablestate(int mode) {
        if (mode == 0 || mode == 2) {
           mClock.setEnabled(false);
           mDate.setEnabled(false);
        } else {
           mClock.setEnabled(true);
           mDate.setEnabled(true);
        }
}

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecentsClearAllLocation) {
            int value = Integer.parseInt((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, value, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mRecentsLayoutStylePref) {
            int type = Integer.valueOf((String) newValue);
            int index = mRecentsLayoutStylePref.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_LAYOUT_STYLE, type);
            mRecentsLayoutStylePref.setSummary(mRecentsLayoutStylePref.getEntries()[index]);
            updateRecentsState(type);
            if (type != 0) { // Disable swipe up gesture, if oreo type selected
                Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
            }
            StagUtils.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mImmersiveRecents) {
                   int mode = Integer.valueOf((String) newValue); 
            Settings.System.putIntForUser(getActivity().getContentResolver(), Settings.System.IMMERSIVE_RECENTS,
                    Integer.parseInt((String) newValue), UserHandle.USER_CURRENT);
            mImmersiveRecents.setValue((String) newValue);
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            updateDisablestate(mode);
            mPreferences = mContext.getSharedPreferences("recent_settings", Activity.MODE_PRIVATE);
            if (!mPreferences.getBoolean("first_info_shown", false) && newValue != null) {
                getActivity().getSharedPreferences("recent_settings", Activity.MODE_PRIVATE)
                        .edit()
                        .putBoolean("first_info_shown", true)
                        .commit();
                openAOSPFirstTimeWarning();
            }
            return true;
    }
        return false;
    }
}
