package com.velopass.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.velopass.app.viewmodels.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()

    when (currentStep) {
        1 -> OnboardingStep1(viewModel) { viewModel.nextStep() }
        2 -> OnboardingStep2(viewModel) { viewModel.nextStep() }
        3 -> OnboardingStep3(viewModel) { viewModel.nextStep() }
        4 -> OnboardingStep4(viewModel) { viewModel.nextStep() }
        5 -> OnboardingStep5(viewModel) { viewModel.nextStep() }
        6 -> OnboardingStep6(viewModel, onOnboardingComplete)
    }
}

@Composable
fun OnboardingStep1(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "VeloPass",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Your Bike Maintenance Companion",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            "Welcome to VeloPass! Select your preferred language to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        ElevatedButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun OnboardingStep2(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Why VeloPass?",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        listOf(
            "Register" to "Keep track of all your bikes",
            "Maintain" to "Never miss maintenance deadlines",
            "Archive" to "Historical bike data"
        ).forEach { (title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun OnboardingStep3(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Create Your Account",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Sign in with Apple")
        }
    }
}

@Composable
fun OnboardingStep4(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var sameCountry by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Complete Your Profile",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it; viewModel.updateDisplayName(it) },
            label = { Text("Display Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = nationality,
            onValueChange = { nationality = it; viewModel.updateNationality(it) },
            label = { Text("Nationality (ISO Code)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sameCountry,
                onCheckedChange = { sameCountry = it; viewModel.setSameCountry(it) }
            )
            Text(
                "Living in same country?",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (!sameCountry) {
            OutlinedTextField(
                value = "",
                onValueChange = { viewModel.updateLegalResidence(it) },
                label = { Text("Legal Residence Country") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ElevatedButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun OnboardingStep5(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Stay Updated",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Enable notifications to get maintenance reminders and important updates about your bikes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        ElevatedButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Enable Notifications")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Skip for Now")
        }
    }
}

@Composable
fun OnboardingStep6(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Register Your First Bike",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Ready to get started? Register your first bike now or skip to explore the dashboard.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        ElevatedButton(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Register First Bike")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Skip to Dashboard")
        }
    }
}
