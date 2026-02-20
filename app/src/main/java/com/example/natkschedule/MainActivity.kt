package com.example.natkschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val PrimaryColor = Color(0xFF2563EB)
val SecondaryColor = Color(0xFFF8FAFC)
val AccentColor = Color(0xFF1E40AF)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.48:5001/") // Твой IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)

        setContent {
            MaterialTheme {
                MainScreen(api)
            }
        }
    }
}

// Главный экран с нижней навигацией
@Composable
fun MainScreen(api: ApiService) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Расписание") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Избранное") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Профиль") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ScheduleScreen(api)
                1 -> PlaceholderScreen("Избранное")
                2 -> PlaceholderScreen("Личный кабинет")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(SecondaryColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            val icon = if (title == "Избранное") Icons.Default.Favorite else Icons.Default.Person

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Раздел «$title»",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(api: ApiService) {
    var groups by remember { mutableStateOf(emptyList<Group>()) }
    var schedule by remember { mutableStateOf(emptyList<ScheduleItem>()) }
    var selectedGroupId by remember { mutableIntStateOf(-1) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try { groups = api.getGroups() } catch (e: Exception) { e.printStackTrace() }
    }

    val groupedSchedule = schedule.groupBy { it.lessonDate }.toSortedMap()
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryColor,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Text(
                "Расписание НАТК",
                modifier = Modifier.padding(20.dp),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Выбор группы (уже работает корректно)
        LazyRow(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups) { group ->
                FilterChip(
                    selected = selectedGroupId == group.id,
                    onClick = {
                        selectedGroupId = group.id
                        scope.launch {
                            try {
                                schedule = emptyList()
                                schedule = api.getSchedule(group.id)
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    },
                    label = { Text(group.name) }
                )
            }
        }

        if (schedule.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Выберите группу", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                groupedSchedule.forEach { (date, lessons) ->
                    item {
                        val prettyDate = date.split("-").reversed().joinToString(".")
                        Text(
                            text = prettyDate,
                            modifier = Modifier.padding(vertical = 12.dp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.DarkGray
                        )
                    }
                    items(lessons.sortedBy { it.lessonNumber }) { lesson ->
                        LessonItem(lesson)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LessonItem(item: ScheduleItem) {
    val time = when(item.lessonNumber) {
        1 -> "08:30 - 10:10"
        2 -> "10:20 - 12:00"
        3 -> "12:30 - 14:10"
        4 -> "14:20 - 16:00"
        else -> "00:00"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = PrimaryColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    item.lessonNumber.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(time, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                Text(item.subjectName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(" ${item.teacherName}", fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("каб. ${item.classroomNumber}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PrimaryColor)
                }
            }
        }
    }
}