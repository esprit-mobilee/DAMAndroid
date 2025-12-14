package com.example.esprit.model
/**
 * Defines all roles available in the ESPRIT app:
 * - STUDENT: standard user, can view and join events/clubs
 * - TEACHER: academic role (future extension)
 * - PARENT: observer role (future extension)
 * - ADMIN: manages users, clubs, events
 * - PRESIDENT: student club leader, can manage members and create events
 */

enum class Role {
    STUDENT,
    TEACHER,
    PARENT,
    PRESIDENT,
    ADMIN
}