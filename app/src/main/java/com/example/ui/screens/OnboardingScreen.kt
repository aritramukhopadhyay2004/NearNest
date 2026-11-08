package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BrandBlack
import com.example.ui.theme.BrandBlue

data class OnboardingStep(
    val title: String,
    val description: String,
    val illustrationRes: Int,
    val buttonText: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Add your Nest",
                description = "Stey connected to your Family. Connect members, find the perfect Nest to stay close that suits your preferences.",
                illustrationRes = R.drawable.illus_onboarding_1,
                buttonText = "Next"
            ),
            OnboardingStep(
                title = "Follow Current Location",
                description = "Save time by finding your family instantly guided by Ai. Ai provide a detailed Timeline of your folks easy to monitor, make your family connected to eachother.",
                illustrationRes = R.drawable.illus_onboarding_2,
                buttonText = "Next"
            ),
            OnboardingStep(
                title = "Explore your family's\nmovement habit",
                description = "Discover the which local places you roam and have visited recently. Experience local life and enjoy authentic experiences in each destination.",
                illustrationRes = R.drawable.illus_onboarding_3,
                buttonText = "Finish"
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Section: Illustration on Blue Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.05f)
                    .statusBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).with(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).with(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboarding_illustration"
                ) { targetIndex ->
                    Image(
                        painter = painterResource(id = steps[targetIndex].illustrationRes),
                        contentDescription = steps[targetIndex].title,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .fillMaxHeight(0.92f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Bottom Section: White Card with Rounded Top Corners
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp)
                        .padding(top = 36.dp, bottom = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = currentStepIndex,
                            transitionSpec = {
                                (fadeIn()).with(fadeOut())
                            },
                            label = "onboarding_text"
                        ) { targetIndex ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = steps[targetIndex].title,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlack,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 34.sp
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = steps[targetIndex].description,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4B5563),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                } else {
                                    onFinish()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlack,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = step.buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Page indicator dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.forEachIndexed { index, _ ->
                                val isActive = index == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) BrandBlue else Color(0xFFD1D5DB))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

