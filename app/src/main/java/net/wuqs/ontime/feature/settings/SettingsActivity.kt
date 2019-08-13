package net.wuqs.ontime.feature.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import net.wuqs.ontime.R
import net.wuqs.ontime.util.logV

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val snoozeLengthsKey = getString(R.string.pref_key_snooze_lengths)
            val s = findPreference<EditTextPreference>(snoozeLengthsKey)
            s?.summaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
                preference.text
            }
            s?.setOnPreferenceChangeListener { pref, newValue ->
                pref as EditTextPreference
                if ((newValue as? String).isNullOrBlank()) {
                    logV("blank")
                    pref.text = getString(R.string.default_snooze_lengths)
                }
                true
            }
        }
    }
}