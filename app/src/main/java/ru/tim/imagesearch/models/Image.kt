package ru.tim.imagesearch.models

import android.os.Parcel
import android.os.Parcelable

data class Image(
        val thumbnail: String?,
        val original: String?,
        val source: String?,
        val title: String?,
        val link: String?) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeString(thumbnail)
                parcel.writeString(original)
                parcel.writeString(source)
                parcel.writeString(title)
                parcel.writeString(link)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<Image> {
                override fun createFromParcel(parcel: Parcel): Image {
                        return Image(parcel)
                }

                override fun newArray(size: Int): Array<Image?> {
                        return arrayOfNulls(size)
                }
        }
}