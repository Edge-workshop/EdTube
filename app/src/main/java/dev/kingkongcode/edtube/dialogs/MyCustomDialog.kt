package dev.kingkongcode.edtube.dialogs

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

class MyCustomDialog(private val user: ETUser) : DialogFragment() {

    private inner class ViewHolder {
        lateinit var ivProfilePic: ImageView
        lateinit var tvFirstName: TextView
        lateinit var tvLastName: TextView
        lateinit var tvEmail: TextView
        lateinit var btnOk: Button
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //getDialog()!!.getWindow()?.setBackgroundDrawableResource(R.drawable.round_corner)
        val viewHolder = ViewHolder()
        val convertView = inflater.inflate(R.layout.profil_dialog, container, false)

        viewHolder.apply {
            ivProfilePic = convertView.findViewById(R.id.ivProfileAvatar)
            tvFirstName = convertView.findViewById(R.id.tvFirstName)
            tvLastName = convertView.findViewById(R.id.tvLastName)
            tvEmail = convertView.findViewById(R.id.tvEmail)
            btnOk = convertView.findViewById(R.id.btnOK)

            //Code to retrieve profile pic from google sign in or else default pic
            Glide.with(this@MyCustomDialog).load(user.userPhoto).
            diskCacheStrategy(DiskCacheStrategy.NONE).
            error(R.drawable.profile_pic_na).into(ivProfilePic)
            tvFirstName.text = user.firstName
            tvLastName.text = user.lastName
            tvEmail.text = user.email

            btnOk.setOnClickListener {
                dismiss()
            }
        }

        return convertView
    }
}