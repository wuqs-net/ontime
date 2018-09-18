package net.wuqs.ontime.feature.about

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*
import net.wuqs.ontime.BuildConfig
import net.wuqs.ontime.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        tv_app_version.text = BuildConfig.VERSION_NAME
    }
}
