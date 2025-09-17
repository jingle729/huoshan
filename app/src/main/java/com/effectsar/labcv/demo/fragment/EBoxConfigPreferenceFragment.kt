package com.effectsar.labcv.demo.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.effectsar.labcv.demo.R
import com.volcengine.ebox.loader.EBoxSDKManager
import com.volcengine.ebox.loader.ServerEnvironment
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.widget.EOToaster

class EBoxConfigPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_ebox_config, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<ListPreference>("eo_ebox_enable_type")?.apply {
            val typeList = arrayOf("开启", "关闭", "根据系统语言")
            val values = arrayOf("open", "close", "follow_system")
            entries = typeList
            entryValues = values

            if (value.isNullOrEmpty()) {
                setValueIndex(2)
            } else {
                val index = values.indexOf(value)
                setValueIndex(index)
            }

            summary = "当前类型: ${typeList[values.indexOf(value)]}"

            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = "当前类型: ${typeList[values.indexOf(newValue.toString())]}"
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<EditTextPreference>("eo_ebox_app_id")?.apply {
            summary = text ?: "默认值"
            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<EditTextPreference>("eo_ebox_app_secret")?.apply {
            summary = text ?: "默认值"
            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<EditTextPreference>("eo_ebox_app_version")?.apply {
            summary = text ?: EBoxSDKManager.resourceConfig.appConfig?.appVersion
            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>("eo_ebox_environment_key")?.apply {
            val typeList = arrayOf("测试环境", "正式环境")
            val values = arrayOf("test", "release")
            entries = typeList
            entryValues = values
            val appEnvironment = EBoxSDKManager.resourceConfig.appConfig?.appEnvironment
            if (appEnvironment == "test") {
                setValueIndex(0)
            }
            if (appEnvironment == "release") {
                setValueIndex(1)
            }
            summary = typeList[values.indexOf(value)]

            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<ListPreference>("eo_ebox_server_environment")?.apply {
            val typeList = arrayOf("线上", "PPE", "BOE")
            val values = arrayOf("online", "ppe", "boe")
            entries = typeList
            entryValues = values

            val serverEnvironment = EBoxSDKManager.resourceConfig.appConfig?.serverEnvironment ?: ServerEnvironment.ONLINE
            when (serverEnvironment) {
                ServerEnvironment.ONLINE -> {
                    setValueIndex(0)
                }

                ServerEnvironment.PPE -> {
                    setValueIndex(1)
                }

                ServerEnvironment.BOE -> {
                    setValueIndex(2)
                }
            }
            summary = typeList[values.indexOf(value)]

            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<EditTextPreference>("eo_ebox_environment_channel")?.apply {
            val environmentChannel = EBoxSDKManager.resourceConfig.appConfig?.environmentChannel
            summary = if (text.isNullOrEmpty().not()) text else environmentChannel
            setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                return@setOnPreferenceChangeListener true
            }
        }

        findPreference<Preference>("eo_ebox_config_restart")?.apply {
            setOnPreferenceClickListener { preference ->
                showRestartDialog()
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun showRestartDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("需要重启来更新EBox配置, 是否现在重启?")
        alertDialogBuilder.setPositiveButton("现在") { dialog, _ ->
            dialog.dismiss()
            restartApp()
        }
        alertDialogBuilder.setNegativeButton("稍后") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun restartApp() {
        val restartIntent = Intent().apply {
            // 设置隐式匹配规则
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            `package` = AppSingleton.instance.packageName
        }
        // 确保Intent可以解析
        if (restartIntent.resolveActivity(AppSingleton.instance.packageManager) != null) {
            startActivity(restartIntent)
            android.os.Process.killProcess(android.os.Process.myPid())
        } else {
            EOToaster.show(AppSingleton.instance, "重启失败")
        }
    }
}