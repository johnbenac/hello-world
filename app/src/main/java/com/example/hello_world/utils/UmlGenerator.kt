//package com.example.hello_world.utils
//
//import com.example.hello_world.models.ConversationMessage
//import com.example.hello_world.models.ConfigPack
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.runtime.Composable
//import com.example.hello_world.*
//import com.example.hello_world.MainActivity
//import com.example.hello_world.MainViewModel
//import com.example.hello_world.services.media_playback.AndroidMediaPlaybackManager
//import io.github.kelvindev15.kotlin2plantuml.plantuml.ClassDiagram
//import io.github.kelvindev15.kotlin2plantuml.plantuml.Configuration
//import java.io.File
//import kotlin.reflect.KVisibility
//
//@ExperimentalMaterial3Api
//@Composable
//fun mains() {
//    generateUml()
//}
//@ExperimentalMaterial3Api
//@Composable
//fun generateUml() {
//    // List all the classes in the package
//    val classesInPackage = listOf(
//        AndroidMediaPlaybackManager::class,
//        AndroidTextToSpeechService::class,
//        Conversation::class,
//        ConversationManager::class,
//        com.example.hello_world.models.ConversationMessage::class,
//        ConversationsManager::class,
////        EditSettingsScreen::class,
//        ElevenLabsTextToSpeechService::class,
//        IConversationRepository::class,
//        LocalConversationDao::class,
//        LocalConversationDatabase::class,
//        LocalConversationEntity::class,
//        LocalConversationMessageEntity::class,
//        LocalRoomConversationRepository::class,
//        MainActivity::class,
////        MainScreen::class,
//        MainViewModel::class,
////        MediaControls::class,
//        MediaPlaybackManager::class,
////        MessageCard::class,
//        OpenAiApiResponse::class,
//        OpenAiApiService::class,
//        com.example.hello_world.models.ConfigPack::class,
////        SavedConversationsScreen::class,
//        SavedConversationsViewModel::class,
////        SettingsScreen::class,
//        SettingsViewModel::class,
//        VoiceTriggerDetector::class,
//        TextToSpeechService::class
//
//        )
//
//    // Configure the visibility settings
//    val configuration = Configuration(maxFieldVisibility = KVisibility.PRIVATE, maxMethodVisibility = KVisibility.PUBLIC)
//
//    // Create a ClassDiagram with all the classes in the package
//    val classDiagram = ClassDiagram(*classesInPackage.toTypedArray(), configuration = configuration)
//
//    // Generate the PlantUML code for the class diagram
//    val myPlantUML = classDiagram.plantUml()
//
//    // Save contents to a file
//    File("output.puml").writeText("$myPlantUML")
//}