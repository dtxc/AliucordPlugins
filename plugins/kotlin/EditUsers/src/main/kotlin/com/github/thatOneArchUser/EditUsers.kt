package com.github.thatOneArchUser

import android.content.Context
import android.os.Environment
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.discord.api.commands.ApplicationCommandType
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.before
import com.discord.models.user.CoreUser
import org.json.JSONObject
import java.io.File

@SuppressWarnings("unused")
@AliucordPlugin(requiresRestart = false)
class EditUsers : Plugin() {
    override fun start(context: Context) {
        val eStorage = Environment.getExternalStorageDirectory()
        val f = File("${eStorage}/Aliucord/settings/editUsers.json")
        if (!f.exists()) {
            f.createNewFile()
            f.writeText("{ }")
        }
        val json = JSONObject(f.readText())
        patcher.before<CoreUser>("getUsername") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getString("username") != "null") {
                    if (id == key.toLong()) it.result = sub.getString("username")
                }
            }
        }
        patcher.before<CoreUser>("getDiscriminator") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getString("discriminator").toIntOrNull() != null) {
                    if (id == key.toLong()) it.result = sub.getString("discriminator").toInt()
                }
            }
        }
        patcher.before<CoreUser>("getBio") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getString("bio") != "null") {
                    if (id == key.toLong()) it.result = sub.getString("bio")
                }
            }
        }
        patcher.before<CoreUser>("isSystemUser") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getBoolean("isSystemUser")) {
                    if (id == key.toLong()) it.result = true
                }
            }
        }
        patcher.before<CoreUser>("isBot") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getBoolean("isBot")) {
                    if (id == key.toLong()) it.result = true
                }
            }
        }
        patcher.before<CoreUser>("getFlags") {
            for (key in json.keys()) {
                val sub = json.getJSONObject(key)
                if (sub.getInt("flags") != 0) {
                    if (id == key.toLong()) it.result = sub.getInt("flags")
                }
            }
        }

        commands.registerCommand("edituser", "edits the info of a user (restart required)", listOf(
            Utils.createCommandOption(ApplicationCommandType.USER, "user", "user to edit", null, true),
            Utils.createCommandOption(ApplicationCommandType.STRING, "username", "changes username", null, false),
            Utils.createCommandOption(ApplicationCommandType.STRING, "discriminator", "changes user discriminator", null, false),
            Utils.createCommandOption(ApplicationCommandType.STRING, "bio", "changes user bio", null, false),
            Utils.createCommandOption(ApplicationCommandType.BOOLEAN, "isSystemUser", "if true, adds system tag to user", null, false),
            Utils.createCommandOption(ApplicationCommandType.BOOLEAN, "isBot", "if true, adds bot tag to user", null, false),
            Utils.createCommandOption(ApplicationCommandType.INTEGER, "flags", "aka user badges (discord.com/developers/docs/resources/user for more info)", null, false)
        )) {ctx ->
            if (!ctx.containsArg("username") && !ctx.containsArg("discriminator") && !ctx.containsArg("bio") && !ctx.containsArg("isSystemUser") && !ctx.containsArg("hasNitro") && !ctx.containsArg("flags")) {
                return@registerCommand CommandsAPI.CommandResult("you didn't pass any arguments", null, false)
            }
            val user = ctx.getUser("user")
            val username = ctx.getStringOrDefault("username", "null")
            val discriminator = ctx.getStringOrDefault("discriminator", "null")
            val bio = ctx.getStringOrDefault("bio", "null")
            val isSystemUser = ctx.getBoolOrDefault("isSystemUser", false)
            val isBot = ctx.getBoolOrDefault("isBot", false)
            val flags = ctx.getIntOrDefault("flags", 0)
            if (bio.length > 190) {
                return@registerCommand CommandsAPI.CommandResult("bio length must be less than 190", null, false)
            }
            if (discriminator != "null" && discriminator.toIntOrNull()!! > 9999) {
                return@registerCommand CommandsAPI.CommandResult("invalid discriminator", null, false)
            }
            if (json.has(user?.id.toString())) {
                json.remove(user?.id.toString())
            }
            json.put(user?.id.toString(), JSONObject())
            val sub = json.getJSONObject(user?.id.toString())
            sub.put("username", username)
            sub.put("discriminator", discriminator)
            sub.put("bio", bio)
            sub.put("isSystemUser", isSystemUser)
            sub.put("isBot", isBot)
            sub.put("flags", flags)
            f.writeText(json.toString())
            CommandsAPI.CommandResult("edited user successfully", null, false)
        }
        commands.registerCommand("resetusers", "removes your edited user info") {
            f.delete()
            CommandsAPI.CommandResult("reset users successfully", null, false)
        }
        commands.registerCommand("reset", "resets a user's modified data", listOf(
            Utils.createCommandOption(ApplicationCommandType.USER, "user", null, null, true)
        )) { ctx ->
            val user = ctx.getUser("user")
            json.remove(user?.id.toString())
            f.writeText(json.toString())
            CommandsAPI.CommandResult("successfully reset ${user?.username}", null, false)
        }
    }
    override fun stop(context: Context) {
        commands.unregisterAll()
    }
}
