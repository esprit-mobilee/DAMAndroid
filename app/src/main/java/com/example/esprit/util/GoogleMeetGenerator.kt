package com.example.esprit.util

import java.util.Random

object GoogleMeetGenerator {
    fun generateLink(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz"
        val part1 = (1..3).map { chars[Random().nextInt(chars.length)] }.joinToString("")
        val part2 = (1..4).map { chars[Random().nextInt(chars.length)] }.joinToString("")
        val part3 = (1..3).map { chars[Random().nextInt(chars.length)] }.joinToString("")
        
        return "https://meet.google.com/$part1-$part2-$part3"
    }
}
