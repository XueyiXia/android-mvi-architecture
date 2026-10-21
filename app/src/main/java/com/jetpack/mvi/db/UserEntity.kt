package com.jetpack.mvi.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author: xiaxueyi
 * @date: 2025-10-21
 * @time: 17:00
 * @说明: 用户数据库实体
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String
)
