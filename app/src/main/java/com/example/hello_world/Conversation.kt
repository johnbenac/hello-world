import com.example.hello_world.Profile
import java.util.UUID

data class Conversation(
    val id: UUID = UUID.randomUUID(),
    val messages: MutableList<ConversationMessage> = mutableListOf(),
    val profile: Profile,
    val createdAt: Long = System.currentTimeMillis(),
    val title: String? = null
)