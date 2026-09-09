package com.aware.app

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.aware.app.ui.theme.CozyPalette
import com.aware.app.ui.theme.Skin

/** Keeps the launcher identity in step with the visual identity selected in Settings. */
object LauncherIconManager {
    private const val OAT = ".launcher.OatLauncher"
    private const val SAGE = ".launcher.SageRoseLauncher"
    private const val PLUM = ".launcher.PlumHearthLauncher"
    private const val LINEN = ".launcher.LinenCafeLauncher"
    private const val NAVY = ".launcher.NavyTideLauncher"
    private const val CHARCOAL = ".launcher.CharcoalLeatherLauncher"
    private const val MAXIMAL = ".launcher.MaximalLauncher"
    private val aliases = listOf(OAT, SAGE, PLUM, LINEN, NAVY, CHARCOAL, MAXIMAL)

    fun sync(
        context: Context,
        skin: Skin,
        cozyPalette: CozyPalette,
        keepEnabledClassName: String? = null,
    ) {
        val selected = when {
            skin == Skin.MAXIMAL -> MAXIMAL
            cozyPalette == CozyPalette.SAGE_ROSE -> SAGE
            cozyPalette == CozyPalette.PLUM_HEARTH -> PLUM
            cozyPalette == CozyPalette.LINEN_CAFE -> LINEN
            cozyPalette == CozyPalette.NAVY_TIDE -> NAVY
            cozyPalette == CozyPalette.CHARCOAL_LEATHER -> CHARCOAL
            else -> OAT
        }
        val packageManager = context.packageManager

        // Enable the destination first so the app always retains a launcher entry.
        setEnabled(packageManager, context, selected, true)
        aliases.filterNot { alias ->
            alias == selected || context.packageName + alias == keepEnabledClassName
        }.forEach { alias ->
            setEnabled(packageManager, context, alias, false)
        }
    }

    private fun setEnabled(
        packageManager: PackageManager,
        context: Context,
        alias: String,
        enabled: Boolean,
    ) {
        val component = ComponentName(context.packageName, context.packageName + alias)
        val desiredState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        if (packageManager.getComponentEnabledSetting(component) != desiredState) {
            packageManager.setComponentEnabledSetting(
                component,
                desiredState,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
