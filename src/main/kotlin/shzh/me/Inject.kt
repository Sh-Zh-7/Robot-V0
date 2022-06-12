package shzh.me

import kotlinx.serialization.json.Json
import org.ktorm.database.Database

val format = Json { ignoreUnknownKeys = true }

val db = Database.connect(
    url = "jdbc:postgresql://db:5432/robot",
    user = "postgres"
)