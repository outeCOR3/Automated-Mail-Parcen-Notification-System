import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import org.example.project.model.Lockers
import org.example.project.model.UsersDTO

@Composable
fun UserListScreen(client: HttpClient, token: String) {
    val users = remember { mutableStateOf<List<UsersDTO>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(true) }
    var lockers by remember { mutableStateOf<Lockers?>(null) }
    LaunchedEffect(Unit) {
        try {
            val response: HttpResponse = client.get("http://192.168.8.132:8080/users/role/User") {
                header("Authorization", "Bearer $token")
            }
            println("UserList Response Status: ${response.status}")
            println("UserList Response Body: ${response.body<String>()}")
            if (response.status == HttpStatusCode.OK) {
                val userList = Json.decodeFromString<List<UsersDTO>>(response.body())
                users.value = userList
            } else {
                println("Failed to fetch users: ${response.status}")
            }
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
                    itemsIndexed(users.value) { index, user ->
                        Text(
                            text = "TENANT ${index + 1}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = user.username, fontSize = 16.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
