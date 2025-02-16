/*
 * Copyright (c) 2021 Auxio Project
 * RootPreferenceFragment.kt is part of Auxio.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.oxycblt.auxio.settings

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import org.oxycblt.auxio.R
import org.oxycblt.auxio.music.MusicViewModel
import org.oxycblt.auxio.playback.PlaybackViewModel
import org.oxycblt.auxio.settings.ui.WrappedDialogPreference
import org.oxycblt.auxio.util.logD
import org.oxycblt.auxio.util.navigateSafe
import org.oxycblt.auxio.util.showToast

/**
 * The [PreferenceFragmentCompat] that displays the root settings list.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@AndroidEntryPoint
class RootPreferenceFragment : BasePreferenceFragment(R.xml.preferences_root) {
    private val playbackModel: PlaybackViewModel by activityViewModels()
    private val musicModel: MusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onOpenDialogPreference(preference: WrappedDialogPreference) {
        if (preference.key == getString(R.string.set_key_music_dirs)) {
            findNavController().navigateSafe(RootPreferenceFragmentDirections.musicDirsSettings())
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        // Hook generic preferences to their specified preferences
        // TODO: These seem like good things to put into a side navigation view, if I choose to
        //  do one.
        when (preference.key) {
            getString(R.string.set_key_ui) -> {
                logD("Navigating to UI preferences")
                findNavController().navigateSafe(RootPreferenceFragmentDirections.uiPreferences())
            }
            getString(R.string.set_key_personalize) -> {
                logD("Navigating to personalization preferences")
                findNavController()
                    .navigateSafe(RootPreferenceFragmentDirections.personalizePreferences())
            }
            getString(R.string.set_key_music) -> {
                logD("Navigating to music preferences")
                findNavController()
                    .navigateSafe(RootPreferenceFragmentDirections.musicPreferences())
            }
            getString(R.string.set_key_audio) -> {
                logD("Navigating to audio preferences")
                findNavController().navigateSafe(RootPreferenceFragmentDirections.audioPeferences())
            }
            getString(R.string.set_key_reindex) -> musicModel.refresh()
            getString(R.string.set_key_rescan) -> musicModel.rescan()
            getString(R.string.set_key_save_state) -> {
                playbackModel.savePlaybackState { saved ->
                    // Use the nullable context, as we could try to show a toast when this
                    // fragment is no longer attached.
                    logD("Showing saving confirmation")
                    if (saved) {
                        context?.showToast(R.string.lbl_state_saved)
                    } else {
                        context?.showToast(R.string.err_did_not_save)
                    }
                }
            }
            getString(R.string.set_key_wipe_state) -> {
                playbackModel.wipePlaybackState { wiped ->
                    logD("Showing wipe confirmation")
                    if (wiped) {
                        // Use the nullable context, as we could try to show a toast when this
                        // fragment is no longer attached.
                        context?.showToast(R.string.lbl_state_wiped)
                    } else {
                        context?.showToast(R.string.err_did_not_wipe)
                    }
                }
            }
            getString(R.string.set_key_restore_state) ->
                playbackModel.tryRestorePlaybackState { restored ->
                    logD("Showing restore confirmation")
                    if (restored) {
                        // Use the nullable context, as we could try to show a toast when this
                        // fragment is no longer attached.
                        context?.showToast(R.string.lbl_state_restored)
                    } else {
                        context?.showToast(R.string.err_did_not_restore)
                    }
                }
            else -> return super.onPreferenceTreeClick(preference)
        }

        return true
    }
}
