package dev.kingkongcode.edtube.dialogs

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dev.kingkongcode.edtube.R
import dev.kingkongcode.edtube.model.ETUser

class MyCustomDialog(private val user: ETUser, private val activity: Activity) : DialogFragment() {

    private inner class ViewHolder {
        lateinit var ivProfilePic: ImageView
        lateinit var tvFirstName: TextView
        lateinit var tvLastName: TextView
        lateinit var tvemail: TextView
        lateinit var btnOk: Button
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner)
        var viewHolder = ViewHolder()
        var convertView = inflater.inflate(R.layout.profil_dialog, container, false)
        viewHolder.ivProfilePic = convertView.findViewById(R.id.ivProfileAvatar)
        viewHolder.tvFirstName = convertView.findViewById(R.id.tvFirstName)
        viewHolder.tvLastName = convertView.findViewById(R.id.tvLastName)
        viewHolder.tvemail = convertView.findViewById(R.id.tvemail)
        viewHolder.btnOk = convertView.findViewById(R.id.btnOK)

        //Code to retrieve profile pic from google sign in or else default pic
        Glide.with(this).load(user.userPhoto).
        diskCacheStrategy(DiskCacheStrategy.NONE).
        error(R.drawable.profile_pic_na).into(viewHolder.ivProfilePic)

        viewHolder.tvFirstName.text = user.firstName
        viewHolder.tvLastName.text = user.lastName
        viewHolder.tvemail.text = user.email

        viewHolder.btnOk.setOnClickListener {
//            HideSystemUi.hideSystemUi(this.activity)
            dismiss()
        }

        return convertView
    }

}