package com.prototype.whatsaudiorecord.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.jetbrains.annotations.PropertyKey

@Entity(tableName = "recordings")
data class Recording(@PrimaryKey(autoGenerate = true)val id:Int,
                     var fileName:String?, var timeStamp:String?) {

    @Ignore
    constructor(fileName:String?, timeStamp:String?):this(id = 0, fileName, timeStamp)

}



