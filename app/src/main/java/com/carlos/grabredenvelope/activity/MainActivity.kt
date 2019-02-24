package com.carlos.grabredenvelope.activity

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.QueryListener
import cn.bmob.v3.listener.SaveListener
import cn.jpush.android.api.JPushInterface
import com.carlos.cutils.base.CBaseActivity
import com.carlos.cutils.listener.PermissionListener
import com.carlos.cutils.thirdparty.AlipayReward
import com.carlos.cutils.thirdparty.WechatReward
import com.carlos.cutils.util.DeviceUtils
import com.carlos.cutils.util.LogUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.dao.CommonVO
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import com.carlos.grabredenvelope.main.About
import com.carlos.grabredenvelope.util.ControlUse
import com.carlos.grabredenvelope.util.ToastUtils
import com.carlos.grabredenvelope.util.Update
import com.carlos.grabredenvelope.util.Utility

/**
 * Created by 小不点 on 2016/2/14.
 */
open class MainActivity : CBaseActivity(), AccessibilityManager.AccessibilityStateChangeListener {

    private var listView: ListView? = null
    private lateinit var list: Array<String>
    private lateinit var cIntent: Intent
    private var adapter: ArrayAdapter<String>? = null

    //AccessibilityService 管理
    private lateinit var accessibilityManager: AccessibilityManager

    /**
     * 获取 QiangHongBaoService 是否启用状态
     * @return
     */
    private val isServiceEnabled: Boolean
        get() {
            val accessibilityServiceInfoList =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            for (info in accessibilityServiceInfoList) {
                if (info.id == "$packageName/.services.DingdingService") {
//                if (info.id == "$packageName/.services.QiangHongBaoService") {
                    Log.d(TAG, "ture")
                    return true
                }
            }
            Log.d(TAG, "false")
            return false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val controlUse = ControlUse(this@MainActivity)
        if (controlUse.stopUse()) {
            show_dialog()
        }

        val update = Update(this@MainActivity, 1)
        update.update()
        //        if(update.hasNewVersion()){
        //            ControlUse.isStop=true;
        //        }
        //        new DelayedUse(MainActivity.this);

        Log.d(TAG, "oncreate")
        //监听AccessibilityService 变化
        accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        accessibilityManager.addAccessibilityStateChangeListener(this)

        //        Update update=new Update(MainActivity.this,1);
        //        update.update();

        listView = findViewById(R.id.listview)
//        list = ArrayList()
//        list!!.add(s(R.string.menu_service))
//        list!!.add(s(R.string.menu_hongbao_record))
//        list!!.add(s(R.string.menu_qq_hongbao))
//        list!!.add(s(R.string.menu_weixin_hongbao))
//        list!!.add(s(R.string.menu_zhifubao_xiuyixiu))
//        list!!.add(s(R.string.menu_qq_shuayishua))
//        list!!.add(s(R.string.about))
//        list!!.add(s(R.string.advice))
//        list!!.add(s(R.string.update))
        list = resources.getStringArray(R.array.list)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        listView!!.adapter = adapter
        Utility.setListViewHeightBasedOnChildren(listView!!)

        listView!!.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                cIntent = Intent()
                when (position) {
                    0 -> {

                        startActivity(
                            cIntent.setClass(
                                this@MainActivity,
                                WechatEnvelopeActivity::class.java
                            )
                        )

//                        startActivity(cIntent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS))
//
//                        if (isServiceEnabled) {
//                            Toast.makeText(this@MainActivity, "找到抢红包，然后关闭服务。", Toast.LENGTH_SHORT)
//                                .show()
//                        } else {
//                            Toast.makeText(this@MainActivity, "找到抢红包，然后开启服务。", Toast.LENGTH_SHORT)
//                                .show()
//                        }


                    }
                    1 -> {

                        ToastUtils.showToast(applicationContext, "关于")
                        startActivity(cIntent.setClass(this@MainActivity, AboutActivity::class.java))
                    }
                    2 -> {

                        startActivity(cIntent.setClass(this@MainActivity, GithubIssuesActivity::class.java))

                    }
                    3 -> {
                        val update = Update(this@MainActivity, 2)
                        update.update()

                    }

                    4 -> {
                        startActivity(cIntent.setClass(this@MainActivity, RewardActivity::class.java))
//                        AlipayReward(this)
//                        WechatReward(
//                            this)

//                        cIntent.setClass(this@MainActivity, XiuYiXiu::class.java)
//                        startActivity(cIntent)
                    }
                    5 -> ToastUtils.showToast(this@MainActivity, "待开发")
                    6 -> {
                        val registrationId = JPushInterface.getRegistrationID(this)
                        LogUtils.d("1099" + "run:--------->registrationId： $registrationId")

                        JPushInterface.setAlias(this, 1, "xbd")

                        ToastUtils.showToast(applicationContext, "关于")
                        cIntent.setClass(this@MainActivity, About::class.java)
                        startActivity(cIntent)
                    }
                    7 -> ToastUtils.showToast(this@MainActivity, "待开发")
                    8 -> {
                        val update = Update(this@MainActivity, 2)
                        update.update()
                    }
                }
            }

        getPermissions()

    }

    private fun getPermissions() {
        requestPermission(100, object : PermissionListener {
            override fun permissionSuccess() {
                RedEnvelopePreferences.imei = DeviceUtils.getImei(this@MainActivity)
                queryImei()
            }

            override fun permissionFail() {

            }

        }, Manifest.permission.READ_PHONE_STATE)
    }

    private fun queryImei() {
        val imei = RedEnvelopePreferences.imei
        LogUtils.d("ime:" + imei)
        if(imei.isEmpty() or imei.equals("000000000000000")) return
        val bmobQuery = BmobQuery<CommonVO>()
        bmobQuery.getObject(RedEnvelopePreferences.imei, object : QueryListener<CommonVO>() {
            override fun done(commonVO: CommonVO?, e: BmobException?) {
                if(e!=null) {
                    LogUtils.e("error:", e)
                    uploadImei()
                }
            }
        })
    }

    private fun uploadImei() {
        val commonVO = CommonVO(RedEnvelopePreferences.imei)
        commonVO.save(object : SaveListener<String>() {
            override fun done(p0: String?, p1: BmobException?) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //监听AccessibilityService 变化
//        updateServiceStatus()
        adapter!!.notifyDataSetChanged()
        Log.d(TAG, "--->onResume")
        JPushInterface.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        JPushInterface.onPause(this)
    }

    private fun s(id: Int): String {
        return resources.getString(id)
    }


    private fun show_dialog() {
        val dialog = AlertDialog.Builder(this).setTitle("提示")
            .setMessage("本软件设定使用时限已到时间，谢谢使用，请点击确定退出。如想继续用可联系小不点，谢谢！").setCancelable(false)
            .setPositiveButton("确定") { dialog, which ->
                // TODO 自动生成的方法存根
                finish()
            }
            .create()
        //		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show()
    }

    /**
     * 更新当前 QiangHongBaoService 显示状态
     */
    private fun updateServiceStatus() {
        if (isServiceEnabled) {
            list[0] = "关闭服务"
        } else {
            list[0] = "开启服务"
        }
    }

    override fun onDestroy() {
        accessibilityManager!!.removeAccessibilityStateChangeListener(this)
        super.onDestroy()
        Log.d(TAG, "ondestroy")
    }

    override fun onAccessibilityStateChanged(enabled: Boolean) {
        updateServiceStatus()
    }

}
