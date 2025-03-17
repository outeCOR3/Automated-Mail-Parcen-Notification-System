/*package org.example.project.model

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class MailRepository {

    // Add a new mail entry and return its ID
    fun addMail(): Int {
        return transaction {
            val phTime = Instant.now().atZone(ZoneId.of("Asia/Manila")).toInstant()
            Mail.insert {
                it[deliveredAt] = phTime
            } get Mail.id // Retrieve inserted ID
        }
    }

    // Get mail by ID
    fun getMailById(mailId: Int): Int? {
        return transaction {
            Mail.select(Mail.id)
                .where{ Mail.id eq mailId }
                .map { it[Mail.id] }
                .firstOrNull()
        }
    }
}
*/