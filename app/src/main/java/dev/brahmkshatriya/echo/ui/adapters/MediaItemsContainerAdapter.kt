package dev.brahmkshatriya.echo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import dev.brahmkshatriya.echo.data.models.MediaItemsContainer
import dev.brahmkshatriya.echo.data.models.Track
import dev.brahmkshatriya.echo.databinding.ItemCategoryBinding
import dev.brahmkshatriya.echo.databinding.ItemTrackBinding
import dev.brahmkshatriya.echo.ui.player.PlayerHelper.Companion.toTimeString
import dev.brahmkshatriya.echo.ui.utils.loadInto

class MediaItemsContainerAdapter(
    private val lifecycle: Lifecycle,
    private val play: (Track) -> Unit,
) : PagingDataAdapter<MediaItemsContainer, MediaItemsContainerAdapter.MediaItemsContainerHolder>(
    MediaItemsContainerComparator
) {
    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.let {
            when (it) {
                is MediaItemsContainer.Category -> 0
                is MediaItemsContainer.TrackItem -> 1
            }
        } ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        0 -> MediaItemsContainerHolder(
            MediaItemsContainerBinding.Category(
                ItemCategoryBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        )

        else -> MediaItemsContainerHolder(
            MediaItemsContainerBinding.Track(
                ItemTrackBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        )
    }


    override fun onBindViewHolder(holder: MediaItemsContainerHolder, position: Int) {
        val item = getItem(position) ?: return
        when (holder.container) {
            is MediaItemsContainerBinding.Category -> {
                val binding = holder.container.binding
                val category = item as MediaItemsContainer.Category
                binding.textView.text = category.title
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(binding.root.context, HORIZONTAL, false)
                val adapter = MediaItemAdapter(play)
                binding.recyclerView.adapter = adapter
                adapter.submitData(lifecycle, category.list)
            }

            is MediaItemsContainerBinding.Track -> {
                val binding = holder.container.binding
                val track = (item as MediaItemsContainer.TrackItem).track
                binding.root.setOnClickListener { play(track) }

                binding.title.text = track.title

                track.cover?.loadInto(binding.imageView)

                if (track.album == null) {
                    binding.album.visibility = View.GONE
                } else {
                    binding.album.visibility = View.VISIBLE
                    binding.album.text = track.album.title
                }

                if (track.artists.isEmpty()) {
                    binding.artist.visibility = View.GONE
                } else {
                    binding.artist.visibility = View.VISIBLE
                    binding.artist.text = track.artists.joinToString(" ") { it.name }
                }

                if (track.duration == null) {
                    binding.duration.visibility = View.GONE
                } else {
                    binding.duration.visibility = View.VISIBLE
                    binding.duration.text = track.duration.toTimeString()
                }
            }
        }

    }

    sealed class MediaItemsContainerBinding {
        data class Category(val binding: ItemCategoryBinding) : MediaItemsContainerBinding()
        data class Track(val binding: ItemTrackBinding) : MediaItemsContainerBinding()
    }

    class MediaItemsContainerHolder(val container: MediaItemsContainerBinding) :
        RecyclerView.ViewHolder(
            when (container) {
                is MediaItemsContainerBinding.Category -> container.binding.root
                is MediaItemsContainerBinding.Track -> container.binding.root
            }
        )

    companion object MediaItemsContainerComparator : DiffUtil.ItemCallback<MediaItemsContainer>() {

        override fun areItemsTheSame(
            oldItem: MediaItemsContainer,
            newItem: MediaItemsContainer
        ): Boolean {
            return when (oldItem) {
                is MediaItemsContainer.Category -> {
                    val newCategory = newItem as? MediaItemsContainer.Category
                    oldItem.title == newCategory?.title
                }

                is MediaItemsContainer.TrackItem -> {
                    val newTrack = newItem as? MediaItemsContainer.TrackItem
                    oldItem.track.uri == newTrack?.track?.uri
                }
            }
        }

        override fun areContentsTheSame(
            oldItem: MediaItemsContainer,
            newItem: MediaItemsContainer
        ) = areItemsTheSame(oldItem, newItem)
    }
}