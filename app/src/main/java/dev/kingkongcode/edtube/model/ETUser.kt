package dev.kingkongcode.edtube.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import dev.kingkongcode.edtube.app.Constants

data class ETUser (val firstName: String?, val lastName: String?, val email: String?, val userPhoto: Uri?) : Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeParcelable(userPhoto, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ETUser> {
        override fun createFromParcel(parcel: Parcel): ETUser {
            return ETUser(parcel)
        }

        override fun newArray(size: Int): Array<ETUser?> {
            return arrayOfNulls(size)
        }
    }
}