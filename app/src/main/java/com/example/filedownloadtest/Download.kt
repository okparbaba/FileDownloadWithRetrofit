package com.example.filedownloadtest

import android.os.Parcel
import android.os.Parcelable

data class Download(var progress:Int? = null,var currentFileSize:Int? = null,var totalFileSize:Int? = null) :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        progress?.let { parcel.writeInt(it) }
        currentFileSize?.let { parcel.writeInt(it) }
        totalFileSize?.let { parcel.writeInt(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Download> {
        override fun createFromParcel(parcel: Parcel): Download {
            return Download(parcel)
        }

        override fun newArray(size: Int): Array<Download?> {
            return arrayOfNulls(size)
        }
    }
}