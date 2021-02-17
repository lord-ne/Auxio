package org.oxycblt.auxio.coil

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.BindingAdapter
import coil.Coil
import coil.fetch.Fetcher
import coil.request.ImageRequest
import org.oxycblt.auxio.R
import org.oxycblt.auxio.music.Album
import org.oxycblt.auxio.music.Artist
import org.oxycblt.auxio.music.Genre
import org.oxycblt.auxio.music.Song
import org.oxycblt.auxio.settings.SettingsManager

// --- BINDING ADAPTERS ---

/**
 * Bind the album art for a [Song].
 */
@BindingAdapter("albumArt")
fun ImageView.bindAlbumArt(song: Song) {
    load(song.album, R.drawable.ic_song, AlbumArtFetcher(context))
}

/**
 * Bind the album art for an [Album].
 */
@BindingAdapter("albumArt")
fun ImageView.bindAlbumArt(album: Album) {
    load(album, R.drawable.ic_album, AlbumArtFetcher(context))
}

/**
 * Bind the image for an [Artist]
 */
@BindingAdapter("artistImage")
fun ImageView.bindArtistImage(artist: Artist) {
    load(artist, R.drawable.ic_artist, MosaicFetcher(context))
}

/**
 * Bind the image for a [Genre]
 */
@BindingAdapter("genreImage")
fun ImageView.bindGenreImage(genre: Genre) {
    load(genre, R.drawable.ic_genre, MosaicFetcher(context))
}

/**
 * Custom extension function similar to the stock coil load extensions, but handles whether
 * to even show images and custom fetchers.
 */
inline fun <reified T : Any> ImageView.load(
    data: T,
    @DrawableRes error: Int,
    fetcher: Fetcher<T>,
) {
    val settingsManager = SettingsManager.getInstance()

    if (!settingsManager.showCovers) {
        setImageResource(error)

        return
    }

    Coil.imageLoader(context).enqueue(
        ImageRequest.Builder(context)
            .target(this)
            .data(data)
            .fetcher(fetcher)
            .error(error)
            .build()
    )
}

// --- OTHER FUNCTIONS ---

/**
 * Get a bitmap for a [song]. [onDone] will be called when the bitmap is loaded.
 * **This not meant for UIs, instead use the Binding Adapters.**
 * @param onDone What to do with the bitmap when the loading is finished. Bitmap will be null if loading failed/shouldn't occur.
 */
fun loadBitmap(context: Context, song: Song, onDone: (Bitmap?) -> Unit) {
    val settingsManager = SettingsManager.getInstance()

    if (!settingsManager.showCovers) {
        onDone(null)
        return
    }

    Coil.imageLoader(context).enqueue(
        ImageRequest.Builder(context)
            .data(song.album)
            .fetcher(AlbumArtFetcher(context))
            .target(
                onError = { onDone(null) },
                onSuccess = { onDone(it.toBitmap()) }
            )
            .build()
    )
}
