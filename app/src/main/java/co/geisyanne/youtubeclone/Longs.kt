package co.geisyanne.youtubeclone

fun Long.formatTime(): String {
    val minutes: Long = this / 1000 / 60
    val seconds: Long = this / 1000 % 60
    return String.format("%02d:%02d", minutes, seconds)
}