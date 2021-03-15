package dev.kingkongcode.edtube.app

import kotlin.math.ceil

class PaginationList {
    companion object{
        fun showNbrPage(listItemActivities: ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>, currentPage: Int) : Pair<String,Int> {
            val totalOfItems = listItemActivities.size
            val nbrOfItemsPerPage = 4.0
            val totalOfPage: Int = ceil(totalOfItems /nbrOfItemsPerPage).toInt()

            return Pair("$currentPage/$totalOfPage",totalOfPage)
        }

        fun filterPage(listItemActivities: ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>, currentPage: Int) : ArrayList<dev.kingkongcode.edtube.model.PlaylistItem> {
            val filterList = ArrayList<dev.kingkongcode.edtube.model.PlaylistItem>()
            val nbrOfItemsPerPage = 4.0

            val lastIndexInPage: Int = if ((currentPage * nbrOfItemsPerPage)-1 <= listItemActivities.size - 1) {
                ((currentPage * nbrOfItemsPerPage)-1).toInt()
            } else listItemActivities.size - 1

            val firstIndexInPage: Int = (((currentPage * nbrOfItemsPerPage)-1) - nbrOfItemsPerPage).toInt() + 1

            for (i in firstIndexInPage .. lastIndexInPage) {
                val model = listItemActivities[i]
                filterList.add(model)
            }

            return filterList
        }
    }
}