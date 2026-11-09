package com.icanmath.app

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val grade: Int = 0,
    val text: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String = "",
    val tip: String = ""
)

@Serializable
data class Profile(
    val id: String,
    val name: String,
    val age: Int,
    val grade: Int,
    /** 1..6 = avatar pilihan; 0 = otomatis dari nama (data lama). */
    val avatar: Int = 0
)

/** kind: DAILY, RANDOM, MCQ, STORY */
@Serializable
data class Session(
    val profileId: String,
    val date: String,
    val kind: String,
    val grade: Int,
    val correct: Int,
    val total: Int,
    val seconds: Int,
    val countsForRank: Boolean
)

/** game: PUZZLE, COMPARE, GROUP */
@Serializable
data class LogicPlay(
    val profileId: String,
    val date: String,
    val game: String,
    val solved: Int,
    val total: Int
)

@Serializable
data class AppData(
    val version: Int = 1,
    val backupId: String = "",
    val activeProfileId: String? = null,
    val profiles: List<Profile> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val logicPlays: List<LogicPlay> = emptyList()
)
