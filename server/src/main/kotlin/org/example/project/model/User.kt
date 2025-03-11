import org.example.project.model.Roles
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

const val MAX_TEXT_LENGTH = 255

object User : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash")
    val role = enumerationByName("role", MAX_TEXT_LENGTH, Roles::class)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}