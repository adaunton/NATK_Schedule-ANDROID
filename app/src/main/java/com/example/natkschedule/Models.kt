package com.example.natkschedule

data class Group(
    val id: Int,
    val name: String
)

data class ScheduleItem(
    val id: Int,
    val subjectName: String,
    val teacherName: String,
    val classroomNumber: String,
    val lessonNumber: Int,
    val lessonDate: String // Новое поле для группировки
)