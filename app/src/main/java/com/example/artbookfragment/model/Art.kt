package com.example.artbookfragment.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Art(

    @ColumnInfo("title")
    val title: String,

    @ColumnInfo("date")
    val date: String,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray

    ) : java.io.Serializable {

        @PrimaryKey(autoGenerate = true)
        var id=0

    }