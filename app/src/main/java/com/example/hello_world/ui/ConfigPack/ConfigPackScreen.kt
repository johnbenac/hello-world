import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hello_world.ui.ConfigPacks.viewmodel.ConfigPacksViewModel
import androidx.compose.material.RadioButton

import androidx.compose.material.Slider

@Composable
fun ConfigPackScreen(configPacksViewModel: ConfigPacksViewModel, onSettingsSaved: () -> Unit, onCancel: () -> Unit) {
    val editedProfile = configPacksViewModel.editedConfigPack.value // Access the value property here

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Edit Settings", modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = editedProfile.name, // Access the name property from the value
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfileName(newValue) }, // Use newValue instead of it
            label = { Text("Profile Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        OutlinedTextField(
            value = editedProfile.systemMessage.takeIf { it.isNotEmpty() } ?: "I am an AI assistant.",
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfileSystemMessage(newValue) },
            label = { Text("System Message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text("Max Length (20 to 2000)", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Slider(
            value = editedProfile.maxLength.toFloat(),
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfileMaxLength(newValue.toInt()) },
            valueRange = 20f..2000f,
            steps = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text("Temperature", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Slider(
            value = editedProfile.temperature.toFloat(),
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfileTemperature(newValue.toDouble()) },
            valueRange = 0f..1f,
            steps = 10,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text("Frequency Penalty", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Slider(
            value = editedProfile.frequencyPenalty.toFloat(),
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfileFrequencyPenalty(newValue.toDouble()) },
            valueRange = 0f..1f,
            steps = 10,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text("Presence Penalty", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Slider(
            value = editedProfile.presencePenalty.toFloat(),
            onValueChange = { newValue -> configPacksViewModel.updateEditedProfilePresencePenalty(newValue.toDouble()) },
            valueRange = 0f..1f,
            steps = 10,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Text("Model", modifier = Modifier.padding(start = 16.dp, top = 8.dp))
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            val models = listOf("gpt-3.5-turbo", "gpt-4")
            models.forEach { model ->
                Row(
                    Modifier
                        .padding(end = 16.dp)
                        .selectable(
                            selected = (model == editedProfile.model),
                            onClick = { configPacksViewModel.updateEditedProfileModel(model) }
                        )
                ) {
                    RadioButton(
                        selected = (model == editedProfile.model),
                        onClick = { configPacksViewModel.updateEditedProfileModel(model) }
                    )
                    Text(
                        text = model,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }


        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                configPacksViewModel.saveEditedProfile()
                onSettingsSaved()
                Log.d("EditSettingsScreen", "Save button clicked")
            }) {
                Text("Save")
            }

            Button(onClick = {
                onCancel()
                Log.d("EditSettingsScreen", "Cancel button clicked")
            }) {
                Text("Cancel")
            }
        }
    }
    //display the properties of the profile
    
}