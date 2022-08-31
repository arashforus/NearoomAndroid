package com.arashforus.nearroom

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MyDateTime {

    fun gmtToLocal(datesToConvert :String ) : String{
        return gmtToLocal(datesToConvert , "HH:mm")
    }

    fun gmtToLocal(datesToConvert :String, output : String) : String{
        val dateFormatInPut = "yyyy/MM/dd HH:mm:ss"
        val dateFomratOutPut = output
        var dateToReturn:String = datesToConvert
        val sdf = SimpleDateFormat(dateFormatInPut)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val sdfOutPutToSend = SimpleDateFormat(dateFomratOutPut)
        sdfOutPutToSend.timeZone = TimeZone.getDefault()
        try {
            val gmt = sdf.parse(datesToConvert)
            dateToReturn = sdfOutPutToSend.format(gmt)
        } catch ( e: ParseException) {
            e.printStackTrace()
        }
        return dateToReturn
    }

    fun getUnix(datesToConvert :String) : Long{
        val dateFormatInPut = "yyyy/MM/dd HH:mm:ss"
        val sdf = SimpleDateFormat(dateFormatInPut)
        var seconds = 0L
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val gmt = sdf.parse(datesToConvert)
            seconds = gmt.time
        } catch ( e: ParseException) {
            e.printStackTrace()
        }
        return seconds
    }

    fun UTC( output : String) : String{
        val time = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        val outuputTime = SimpleDateFormat(output, Locale.getDefault())
        outuputTime.timeZone = TimeZone.getTimeZone("UTC")
        return outuputTime.format(time)
    }

    fun DayDiff( date  :String) : Int {
        var formatInput = "yyyy/MM/dd HH:mm:ss"
        if (date.contains("-",true)){
            formatInput = "yyyy-MM-dd HH:mm:ss"
        }
        val Formateyear = "yyyy"
        val FormateMonth = "MM"
        val FormateDate = "dd"
        val myHoleDate = SimpleDateFormat(formatInput)
        myHoleDate.timeZone = TimeZone.getTimeZone("GMT")
        val holedate = myHoleDate.parse(date)
        val year = SimpleDateFormat(Formateyear).format(holedate)
        val month = SimpleDateFormat(FormateMonth).format(holedate)
        val day = SimpleDateFormat(FormateDate).format(holedate)

        val daydiff =   (( Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.YEAR) - year.toInt()) * 365 ) +
                        (( Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.MONTH) + 1 - month.toInt()) * 30 ) +
                        (( Calendar.getInstance(TimeZone.getTimeZone("GMT")).get(Calendar.DAY_OF_MONTH) - day.toInt()) * 1 )
        //println(daydiff)
        return daydiff
    }

    fun DayDiff( oldDate  :String , newDate : String) : Int {
        //println("oldDate : $oldDate")
        //println("newDate : $newDate")
        var oldFormatInPut = "yyyy/MM/dd HH:mm:ss"
        if (oldDate.contains("-",true)){
            oldFormatInPut = "yyyy-MM-dd HH:mm:ss"
        }
        var newFormatInPut = "yyyy/MM/dd HH:mm:ss"
        if (newDate.contains("-",true)){
            newFormatInPut = "yyyy-MM-dd HH:mm:ss"
        }
        val Formateyear = "yyyy"
        val FormateMonth = "MM"
        val FormateDate = "dd"
        val oldHoleDateFormat = SimpleDateFormat(oldFormatInPut)
        oldHoleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val oldHoleDate = oldHoleDateFormat.parse(oldDate)
        val oldYear = SimpleDateFormat(Formateyear).format(oldHoleDate).toInt()
        val oldMonth = SimpleDateFormat(FormateMonth).format(oldHoleDate).toInt()
        val oldDay = SimpleDateFormat(FormateDate).format(oldHoleDate).toInt()
        val oldFull = SimpleDateFormat("yyyyMMdd").format(oldHoleDate).toInt()

        val newHoleDateFormat = SimpleDateFormat(newFormatInPut)
        newHoleDateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val newHoleDate = newHoleDateFormat.parse(newDate)
        val newYear = SimpleDateFormat(Formateyear).format(newHoleDate).toInt()
        val newMonth = SimpleDateFormat(FormateMonth).format(newHoleDate).toInt()
        val newDay = SimpleDateFormat(FormateDate).format(newHoleDate).toInt()
        val newFull = SimpleDateFormat("yyyyMMdd").format(newHoleDate).toInt()
        //println("newFull : $newFull")
        //println("oldFull : $oldFull")
        var daydiff = 0
        if ( newFull > oldFull ) {
            daydiff =   (( newYear - oldYear     ) * 365 ) +
                        (( newMonth  - oldMonth  ) * 30 ) +
                        (( newDay - oldDay       ) * 1 )
        }else{
            daydiff =   (( oldYear - newYear     ) * 365 ) +
                        (( oldMonth  - newMonth  ) * 30 ) +
                        (( oldDay - newDay       ) * 1 )
        }

        //println(daydiff)
        return daydiff
    }

    fun secondDiff(date :String) : Int {
        var formatInput = "yyyy/MM/dd HH:mm:ss"
        if (date.contains("-",true)){
            formatInput = "yyyy-MM-dd HH:mm:ss"
        }
        val Formateyear = "yyyy"
        val FormateMonth = "MM"
        val FormateDate = "dd"
        val FormateHour = "HH"
        val FormateMinute = "mm"
        val FormateSecond = "ss"
        val Fromatampm = "a"
        val myHoleDate = SimpleDateFormat(formatInput)
        myHoleDate.timeZone = TimeZone.getTimeZone("GMT")
        val holeDate = myHoleDate.parse(date)
        val year = SimpleDateFormat(Formateyear).format(holeDate)
        val month = SimpleDateFormat(FormateMonth).format(holeDate)
        val day = SimpleDateFormat(FormateDate).format(holeDate)
        val hour = SimpleDateFormat(FormateHour).format(holeDate)
        val minute = SimpleDateFormat(FormateMinute).format(holeDate)
        val second = SimpleDateFormat(FormateSecond).format(holeDate)
        //val ampm = SimpleDateFormat(Fromatampm).format(holeDate)

        //println("-- $year $month $day $hour $minute $second $ampm --")
        //println("-- ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR)}" +
        //        " ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH)}" +
        //        " ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH)}" +
        //        " ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.HOUR_OF_DAY)}" +
        //        " ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE)}" +
        //        " ${Calendar.getInstance(TimeZone.getDefault()).get(Calendar.SECOND)}")

        val secondDiff =
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR) - year.toInt()) * 31104000 ) +
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MONTH) + 1 - month.toInt()) * 2592000 ) +
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_MONTH) - day.toInt()) * 86400 ) +
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.HOUR_OF_DAY) - hour.toInt()) * 3600 ) +
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE) - minute.toInt()) * 60 ) +
                (( Calendar.getInstance(TimeZone.getDefault()).get(Calendar.SECOND) - second.toInt()) * 1 )
        //println("date is $date")
        //println(secondDiff)

        return secondDiff
    }

    fun getLocalClock() : Array<Int>{
        return arrayOf(Calendar.getInstance(TimeZone.getDefault()).get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance(TimeZone.getDefault()).get(Calendar.MINUTE),
                        Calendar.getInstance(TimeZone.getDefault()).get(Calendar.SECOND))
    }

    fun getLocalFullTime() : String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        return sdf.format(Date())
    }

    fun getGMTFullTime() : String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return sdf.format(Date())
    }

    fun getUTCFullTime() : String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun secDiffLocalClock(time2 : Array<Int> , time1 : Array<Int> ) : Int {
        return ( ( (time2[0] - time1[0])*3600) + ( (time2[1] - time1[1])*60) + ( (time2[2] - time1[2])*1)  )
    }

    fun getDayOfWeek( offset : Int ) : String {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var targetDay = today - offset
        if ( targetDay < 0  ){
            targetDay = 7 - targetDay
        }
        val targetDayString = when (targetDay){
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            else -> "Saturday"
        }
        return targetDayString
    }

}