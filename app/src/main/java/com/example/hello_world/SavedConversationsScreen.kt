package com.example.hello_world

import androidx.compose.runtime.Composable
import java.util.UUID

@Composable
fun SavedConversationsScreen(
    viewModel: SavedConversationsViewModel,
    onConversationSelected: (UUID) -> Unit,
    onBack: () -> Unit
) {
    // TODO: Implement the UI for displaying the list of saved conversations
    // TODO: implementing audio export and compilation
}