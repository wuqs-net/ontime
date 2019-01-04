package net.wuqs.ontime.feature.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import net.wuqs.ontime.BuildConfig
import net.wuqs.ontime.R
import net.wuqs.ontime.util.logE
import net.wuqs.ontime.util.logV

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        tv_app_version.text = BuildConfig.VERSION_NAME

        tv_review.setOnClickListener { rate() }
    }

    private fun rate() {
        try {
            val uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
            logV("Opening $uri")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            logE("Cannot open market page for OnTime")
        }
    }
}
