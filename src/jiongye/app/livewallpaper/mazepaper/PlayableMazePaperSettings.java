package jiongye.app.livewallpaper.mazepaper;

import jiongye.app.livewallpaper.mazepaper.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PlayableMazePaperSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getPreferenceManager().setSharedPreferencesName(PlayableMazePaperService.SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.playablemazepaper_setting);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}
}
