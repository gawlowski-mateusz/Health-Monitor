package com.mateusz.frontend

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    CyclingSessionUnitTests::class,
    LoginUnitTests::class,
    ProfileUpdateUnitTests::class,
    RegisterUnitTests::class,
    RunningSessionUnitTests::class,
    StepsCountUpdateTests::class,
    StepsGoalUpdateUnitTests::class,
    WalkingSessionUnitTests::class
)
class AllTestsSuite