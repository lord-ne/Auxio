/*
 * Copyright (c) 2022 Auxio Project
 * QueueViewModel.kt is part of Auxio.
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
 
package org.oxycblt.auxio.playback.queue

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oxycblt.auxio.list.adapter.UpdateInstructions
import org.oxycblt.auxio.music.MusicParent
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.playback.state.PlaybackStateManager
import org.oxycblt.auxio.util.Event
import org.oxycblt.auxio.util.MutableEvent
import org.oxycblt.auxio.util.logD

/**
 * A [ViewModel] that manages the current queue state and allows navigation through the queue.
 *
 * @author Alexander Capehart (OxygenCobalt)
 */
@HiltViewModel
class QueueViewModel @Inject constructor(private val playbackManager: PlaybackStateManager) :
    ViewModel(), PlaybackStateManager.Listener {

    private val _queue = MutableStateFlow(listOf<Song>())
    /** The current queue. */
    val queue: StateFlow<List<Song>> = _queue
    private val _queueInstructions = MutableEvent<UpdateInstructions>()
    /** Instructions for how to update [queue] in the UI. */
    val queueInstructions: Event<UpdateInstructions> = _queueInstructions
    private val _scrollTo = MutableEvent<Int>()
    /** Controls whether the queue should be force-scrolled to a particular location. */
    val scrollTo: Event<Int>
        get() = _scrollTo

    private val _index = MutableStateFlow(playbackManager.queue.index)
    /** The index of the currently playing song in the queue. */
    val index: StateFlow<Int>
        get() = _index

    init {
        playbackManager.addListener(this)
    }

    override fun onIndexMoved(queue: Queue) {
        logD("Index moved, synchronizing and scrolling to new position")
        _scrollTo.put(queue.index)
        _index.value = queue.index
    }

    override fun onQueueChanged(queue: Queue, change: Queue.Change) {
        // Queue changed trivially due to item mo -> Diff queue, stay at current index.
        logD("Updating queue display")
        _queueInstructions.put(change.instructions)
        _queue.value = queue.resolve()
        if (change.type != Queue.Change.Type.MAPPING) {
            // Index changed, make sure it remains updated without actually scrolling to it.
            logD("Index changed with queue, synchronizing new position")
            _index.value = queue.index
        }
    }

    override fun onQueueReordered(queue: Queue) {
        // Queue changed completely -> Replace queue, update index
        logD("Queue changed completely, replacing queue and position")
        _queueInstructions.put(UpdateInstructions.Replace(0))
        _scrollTo.put(queue.index)
        _queue.value = queue.resolve()
        _index.value = queue.index
    }

    override fun onNewPlayback(queue: Queue, parent: MusicParent?) {
        // Entirely new queue -> Replace queue, update index
        logD("New playback, replacing queue and position")
        _queueInstructions.put(UpdateInstructions.Replace(0))
        _scrollTo.put(queue.index)
        _queue.value = queue.resolve()
        _index.value = queue.index
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.removeListener(this)
    }

    /**
     * Start playing the the queue item at the given index.
     *
     * @param adapterIndex The index of the queue item to play. Does nothing if the index is out of
     *   range.
     */
    fun goto(adapterIndex: Int) {
        if (adapterIndex !in queue.value.indices) {
            return
        }
        logD("Going to position $adapterIndex in queue")
        playbackManager.goto(adapterIndex)
    }

    /**
     * Remove a queue item at the given index.
     *
     * @param adapterIndex The index of the queue item to play. Does nothing if the index is out of
     *   range.
     */
    fun removeQueueDataItem(adapterIndex: Int) {
        if (adapterIndex !in queue.value.indices) {
            return
        }
        logD("Removing item $adapterIndex in queue")
        playbackManager.removeQueueItem(adapterIndex)
    }

    /**
     * Move a queue item from one index to another index.
     *
     * @param adapterFrom The index of the queue item to move.
     * @param adapterTo The destination index for the queue item.
     * @return true if the items were moved, false otherwise.
     */
    fun moveQueueDataItems(adapterFrom: Int, adapterTo: Int): Boolean {
        if (adapterFrom !in queue.value.indices || adapterTo !in queue.value.indices) {
            return false
        }
        logD("Moving $adapterFrom to $adapterFrom in queue")
        playbackManager.moveQueueItem(adapterFrom, adapterTo)
        return true
    }
}
