package com.tommasoberlose.anotherwidget.ui.activity

import android.app.Activity
import android.location.Address
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.tommasoberlose.anotherwidget.R
import com.tommasoberlose.anotherwidget.`object`.Constants
import com.tommasoberlose.anotherwidget.`object`.CustomLocationEvent
import kotlinx.android.synthetic.main.activity_choose_application.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.tommasoberlose.anotherwidget.`object`.ApplicationListEvent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.tommasoberlose.anotherwidget.`object`.AppInfoSavedEvent
import com.tommasoberlose.anotherwidget.ui.adapter.ApplicationInfoAdapter


class ChooseApplicationActivity : AppCompatActivity() {
    lateinit var adapter: ApplicationInfoAdapter
    val appList = ArrayList<ApplicationInfo>()
    val appListFiltered = ArrayList<ApplicationInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_application)
        val pm = packageManager

        action_default.setOnClickListener {
            selectDefaultApp()
        }

        action_back.setOnClickListener {
            onBackPressed()
        }

        action_none.setOnClickListener {
            removeClickAction()
        }

        list_view.setHasFixedSize(true);
        val mLayoutManager = LinearLayoutManager(this);
        list_view.layoutManager = mLayoutManager;

        adapter = ApplicationInfoAdapter(this, appListFiltered);
        list_view.setAdapter(adapter);

        location.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                Thread().run {
                    val appsFiltered = if (text == null || text.equals("")) appList else appList.filter { pm.getApplicationLabel(it).toString().contains(text.toString(), true) }
                    EventBus.getDefault().post(ApplicationListEvent(appsFiltered, true))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

    }

    fun selectDefaultApp() {
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.RESULT_APP_NAME, "")
        resultIntent.putExtra(Constants.RESULT_APP_PACKAGE, "")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun removeClickAction() {
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.RESULT_APP_NAME, getString(R.string.action_none))
        resultIntent.putExtra(Constants.RESULT_APP_PACKAGE, "_")
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun multiEventAction() {
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.RESULT_APP_NAME, getString(R.string.action_go_to_next_event))
        resultIntent.putExtra(Constants.RESULT_APP_PACKAGE, Constants.PREF_SHOW_NEXT_EVENT)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun saveApp(e: AppInfoSavedEvent) {
        val pm = packageManager
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.RESULT_APP_NAME, pm.getApplicationLabel(e.app).toString())
        resultIntent.putExtra(Constants.RESULT_APP_PACKAGE, e.app.packageName)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onResume() {
        super.onResume()
        Thread().run {
            val pm = packageManager
            val apps = pm.getInstalledApplications(0)
            EventBus.getDefault().post(ApplicationListEvent(apps, false))
        }
    }

    public override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: ApplicationListEvent) {
        if (!event.filtered) {
            appList.clear()
            event.apps.mapTo(appList, {it})
        }
        appListFiltered.clear()
        event.apps.mapTo(appListFiltered, {it})
        adapter.changeData(appListFiltered)
    }
}
