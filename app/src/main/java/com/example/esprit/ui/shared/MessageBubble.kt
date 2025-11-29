package com.example.esprit.ui.shared



import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.esprit.model.Message
import com.example.esprit.ui.theme.RedPrimary


@Composable
fun MessageBubble(message: Message) {
    val isMine = message.isMine

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isMine) RedPrimary else Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(10.dp),
                color = if (isMine) Color.White else Color.Black
            )
        }
    }
}
