
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json
import org.example.project.model.UsersDTO

@Composable
fun UserListScreen(client: HttpClient) {
    val users = remember { mutableStateOf<List<UsersDTO>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.68.138:8080/users/role/User")
            val responseBody = response.body<String>()
            println("Raw response: $responseBody")

            val userList = Json.decodeFromString<List<UsersDTO>>(responseBody)
            users.value = userList
            isLoading.value = false
        } catch (e: Exception) {
            println("Error fetching users: ${e.message}")
            e.printStackTrace()
            isLoading.value = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            isLoading.value -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF78C2D1)
                )
            }
            users.value.isEmpty() -> {
                Text(
                    text = "No users found",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "User List",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78C2D1),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    itemsIndexed(users.value) { index, user -> // Using itemsIndexed to get index
                        Text(
                            text = "TENANT ${index + 1}", // Adding index
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),  // Padding between cards
                            shape = RoundedCornerShape(16.dp), // Rounded corners for the card
                            backgroundColor = Color.White,
                            elevation = 4.dp  // Elevation for the card
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp) // Padding inside the card
                            ) {

                                Text(
                                    text = user.username, // User's username text
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
