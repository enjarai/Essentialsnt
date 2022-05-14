package nl.enjarai.essentialsnt.config

import com.google.gson.GsonBuilder
import java.io.*
import java.nio.charset.StandardCharsets

interface ModConfig {
    fun getConfigFile(): File

    fun save() {
        saveConfigFile(this, getConfigFile())
    }

    companion object {
        private val gson = GsonBuilder()
            .setPrettyPrinting() // Makes the json use new lines instead of being a "one-liner"
            .serializeNulls() // Makes fields with `null` value to be written as well.
            .disableHtmlEscaping() // We'll be able to use custom chars without them being saved differently
            .create()

        fun <T : ModConfig> loadConfigFile(javaClass: Class<T>): T {
            var config = javaClass.getDeclaredConstructor().newInstance()
            val file = config.getConfigFile()
            if (file.exists()) {
                // An existing config is present, we should use its values
                try {
                    BufferedReader(
                        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8)
                    ).use { fileReader ->
                        // Parses the config file and puts the values into config object
                        val readConfig = gson.fromJson(fileReader, javaClass)
                        // gson.fromJson() can return null if file is empty
                        if (readConfig != null) config = readConfig
                    }
                } catch (e: IOException) {
                    throw RuntimeException("Problem occurred when trying to load config: ", e)
                }
            }

            // Saves the file in order to write new fields if they were added
            config.save()
            return config
        }

        fun <T : ModConfig> saveConfigFile(config: T, file: File) {
            try {
                OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
                    gson.toJson(
                        config,
                        writer
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}