package com.example.timemaster

import android.util.Log

var arrEmail = ArrayList<String>()
var arrUserId = ArrayList<String>()

class uAuth {
    fun StoreUser(UserEmail:String, UserId:String)
    {
        if(arrEmail.size>0)
        {
            arrEmail.clear()
            arrUserId.clear()
        }
        arrEmail.add(UserEmail)
        arrUserId.add(UserId)
        Log.d("OUTPUT", "UserId: $UserId")
    }
}