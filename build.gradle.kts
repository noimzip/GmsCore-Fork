/*
 * SPDX-FileCopyrightText: 2013 microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    val cronetVersion = '102.5005.125'
    val wearableVersion = '0.1.1'

    val kotlinVersion = '2.2.21'
    val coroutineVersion = '1.10.2'

    val annotationVersion = '1.9.1'
    val appcompatVersion = '1.7.1'
    val biometricVersion = '1.1.0'
    val coreVersion = '1.17.0'
    val fragmentVersion = '1.8.9'
    val lifecycleVersion = '2.10.0'
    val loaderVersion = '1.1.0'
    val materialVersion = '1.14.0-alpha07'
    val mediarouterVersion = '1.8.1'
    val multidexVersion = '2.0.1'
    val navigationVersion = '2.9.6'
    val preferenceVersion = '1.2.1'
    val recyclerviewVersion = '1.4.0'
    val webkitVersion = '1.14.0'

    val slf4jVersion = '2.0.17'
    val volleyVersion = '1.2.1'
    val okHttpVersion = '4.12.0'
    val ktorVersion = '2.3.12'
    val wireVersion = '5.4.0'
    val tinkVersion = '1.20.0'

    val androidBuildGradleVersion = '8.13.1'

    val androidBuildVersionTools = '36.0.0'

    val androidMinSdk = 26
    val androidTargetSdk = 36
    val androidCompileSdk = 36

    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:$androidBuildGradleVersion"
        classpath "org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:$kotlinVersion"
        classpath "com.squareup.wire:wire-gradle-plugin:$wireVersion"
    }
}

def execResult(... args) {
    providers.exec { commandLine args }.standardOutput.asTval get()
}

def ignoreGit = providers.environmentVariable('GRADLE_MICROG_VERSION_WITHOUT_GIT').getOrElse('0') == '1'
def gmsVersion = "25.49.32"
def gmsVersionCode = Integer.parseInt(gmsVersion.replaceAll('\\.', ''))
def vendingVersion = "49.1.33"
def vendingVersionCode = Integer.parseInt(vendingVersion.replaceAll('\\.', ''))
def gitVersionBase = !ignoreGit ? execResult('git', 'describe', '--tags', '--abbrev=0', '--match=v[0-9]*').trim().substring(1) : "v0.0.0.$gmsVersionCode"
def gitCommitCount = !ignoreGit ? Integer.parseInt(execResult('git', 'rev-list', '--count', "v$gitVersionBase..HEAD").trim()) : 0
def gitCommitId = !ignoreGit ? execResult('git', 'show-ref', '--abbrev=7', '--head', 'HEAD').trim().split(' ')[0] : '0000000'
def gitDirty = false
if (!ignoreGit) {
  execResult('git', 'status', '--porcelain').lines().each { stat ->
    def status = stat.substring(0,2)
    def file = stat.substring(3)
    if (status == '??') {
      if (subprojects.any { p -> file.startsWith(p.name + '/') }) {
        logger.lifecycle('Dirty file: {} (untracked)', file)
        gitDirty = true
      } else {
        logger.info('New file outside module: {} (ignored for dirty check)', file)
      }
    } else {
      logger.lifecycle('Dirty file: {} (changed)', file)
      gitDirty = true
    }
  }
}
def ourVersionBase = gitVersionBase.substring(0, gitVersionBase.lastIndexOf('.'))
def ourVersionMinor = Integer.parseInt(ourVersionBase.substring(ourVersionBase.lastIndexOf('.') + 1))
def ourGmsVersionCode = gmsVersionCode * 1000 + ourVersionMinor * 2  + (gitCommitCount > 0 || gitDirty ? 1 : 0)
def ourGmsVersionName = "$ourVersionBase.$gmsVersionCode" + (gitCommitCount > 0 && !gitDirty ? "-$gitCommitCount" : "") + (gitDirty ? "-dirty" : "") + (gitCommitCount > 0 && !gitDirty ? " ($gitCommitId)" : "")
def ourVendingVersionCode = 80000000 + vendingVersionCode * 100 + ourVersionMinor * 2  + (gitCommitCount > 0 || gitDirty ? 1 : 0)
def ourVendingVersionName = "$ourVersionBase.$vendingVersionCode" + (gitCommitCount > 0 && !gitDirty ? "-$gitCommitCount" : "") + (gitDirty ? "-dirty" : "") + (gitCommitCount > 0 && !gitDirty ? " ($gitCommitId)" : "")
logger.lifecycle('Starting build for GMS version {} ({})...', ourGmsVersionName, ourGmsVersionCode)

allprojects {
    apply plugin: 'idea'

    group = 'org.microg.gms'
    version = ourGmsVersionName
    val vendingAppVersionName = ourVendingVersionName
    val vendingAppVersionCode = ourVendingVersionCode
    val appVersionCode = ourGmsVersionCode
    val isReleaseVersion = false
}

subprojects {
    repositories {
        mavenCentral()
        google()
        if (hasModule("hms", false)) maven {url 'https://developer.huawei.com/repo/'}
    }
}
