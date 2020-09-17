package dev.kingkongcode.edtube.util

import android.content.Context
import com.google.api.services.youtube.model.PlaylistItem
import kotlin.math.ceil

class PaginationList {

    companion object{

//        fun showNbrPage(mContext: Context, listItems: ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>, currentPage: Int) : String{
//            val totalOfItems = listItems.size
//            val nbrOfItemsPerPage = 4.0
//            val totalOfPage: Int = ceil(totalOfItems /nbrOfItemsPerPage).toInt()
//
//            return "$currentPage/$totalOfPage"
//        }

        fun showNbrPage(mContext: Context, listItems: ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>, currentPage: Int) : Pair<String,Int>{
            val totalOfItems = listItems.size
            val nbrOfItemsPerPage = 4.0
            val totalOfPage: Int = ceil(totalOfItems /nbrOfItemsPerPage).toInt()

            return Pair("$currentPage/$totalOfPage",totalOfPage)
        }

        fun filterPage(mContext: Context, listItems: ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>, currentPage: Int) : ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>{
            var filterList = ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>()

            //val maxIndex = listItems.size - 1
            val nbrOfItemsPerPage = 4.0

            val lastIndexInPage: Int = if ((currentPage * nbrOfItemsPerPage)-1 <= listItems.size - 1){
                ((currentPage * nbrOfItemsPerPage)-1).toInt()
            } else listItems.size - 1

            val firstIndexInPage: Int = (((currentPage * nbrOfItemsPerPage)-1) - nbrOfItemsPerPage).toInt() + 1

            for (i in firstIndexInPage .. lastIndexInPage ){
                val model = listItems[i]
                filterList.add(model)
            }

            return filterList
        }

    }





}