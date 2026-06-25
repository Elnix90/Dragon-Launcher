package org.elnix.dragonlauncher.base.model.models

import org.elnix.dragonlauncher.i18n.R

public interface ADBCommands {
    public val commandEnable: String
    public val commandDisable: String
    public val resId: Int
}

public enum class BluetoothADBCommands(
    override val commandEnable: String,
    override val commandDisable: String,
    override val resId: Int
) : ADBCommands {
    Cmd(
        commandEnable = "cmd bluetooth_manager enable",
        commandDisable = "cmd bluetooth_manager disable",
        resId = R.string.shizuku_bt_cmd_cmd
    ),
    ServiceCall(
        commandEnable = "service call bluetooth_manager 6",
        commandDisable = "service call bluetooth_manager 8",
        resId = R.string.shizuku_bt_service_call
    ),
    Root(
        commandEnable = "su -c \"service call bluetooth_manager 6\"",
        commandDisable = "su -c \"service call bluetooth_manager 8\"",
        resId = R.string.shizuku_bt_root
    ),
    Intent(
        commandEnable = "am start -a android.bluetooth.adapter.action.REQUEST_ENABLE",
        commandDisable = "am start -a android.bluetooth.adapter.action.REQUEST_DISABLE",
        resId = R.string.shizuku_bt_intent
    )
}

public enum class WifiADBCommands(
    override val commandEnable: String,
    override val commandDisable: String,
    override val resId: Int
) : ADBCommands {
    Svc(
        commandEnable = "svc wifi enable",
        commandDisable = "svc wifi disable",
        resId = R.string.shizuku_wifi_svc
    )
}

public enum class DataADBCommands(
    override val commandEnable: String,
    override val commandDisable: String,
    override val resId: Int
) : ADBCommands {
    Svc(
        commandEnable = "svc data enable",
        commandDisable = "svc data disable",
        resId = R.string.shizuku_data_svc
    ),
    Settings(
        commandEnable = "settings put global mobile_data 1",
        commandDisable = "settings put global mobile_data 0",
        resId = R.string.shizuku_data_settings
    )
}