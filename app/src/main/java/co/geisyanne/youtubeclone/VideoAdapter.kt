package co.geisyanne.youtubeclone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class VideoAdapter(private val videos: List<Video>, val onClick: (Video) -> Unit) :
    RecyclerView.Adapter<VideoAdapter.VideoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder =
        VideoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_video,
                parent,
                false
            )
        )


    override fun getItemCount(): Int = videos.size

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        holder.bind(videos[position])
    }

    inner class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(video: Video) {
            with(itemView) {
                setOnClickListener {
                    onClick.invoke(video)
                }
                Picasso.get().load(video.thumbnailUrl)
                    .into(itemView.findViewById<ImageView>(R.id.video_thumbnail))
                Picasso.get().load(video.publisher.pictureProfileUrl)
                    .into(itemView.findViewById<ImageView>(R.id.video_photo_author))
                itemView.findViewById<TextView>(R.id.video_title).text = video.title
                itemView.findViewById<TextView>(R.id.video_subtitle).text = context.getString(
                    R.string.info,
                    video.publisher.name, video.viewsCountLabel, video.publishedAt.formatted()
                )
            }
        }
    }

}