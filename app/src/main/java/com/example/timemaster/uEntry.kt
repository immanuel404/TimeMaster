package com.example.timemaster

var db_Count = 0
var arrFilterStart = ArrayList<String>()
var arrFilterEnd = ArrayList<String>()
var arrFilterMin = ArrayList<String>()
var arrFilterMax = ArrayList<String>()

var arrId = ArrayList<String>()
var arrDate = ArrayList<String>()
var arrStart = ArrayList<String>()
var arrEnd = ArrayList<String>()
var arrMin = ArrayList<String>()
var arrMax = ArrayList<String>()
var arrPay = ArrayList<String>()
var arrTax = ArrayList<String>()
var arrDescription = ArrayList<String>()
var arrCategory = ArrayList<String>()
var arrImage = ArrayList<String>()

data class uEntry
    (
    var Id:String? = null,
    var Date:String? = null,
    var Start:String? = null,
    var End:String? = null,
    var Min:String? = null,
    var Max:String? = null,
    var Pay:String? = null,
    var Tax:String? = null,
    var Description:String? = null,
    var Category:String? = null,
    var ImageUrl:String? = null
)