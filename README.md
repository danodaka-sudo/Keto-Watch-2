# KETO WATCH 🥑

Android app that lets you **photograph your food**, identifies it (including **Ugandan cuisine** and products from
**Ugandan supermarkets and shops**), estimates the **calories**, and tells you whether it is **keto friendly** and how it
affects today's keto carb budget.

## Features

| | |
|---|---|
| 📷 **Snap & identify** | Take a photo or pick one from the gallery. Claude AI vision recognises the dish and each component on the plate (e.g. *matooke + binyebwa*, *rolex*, *chicken luwombo*, *malakwang*, *nsenene*, *Nile Special*, *Riham Cola*). |
| 🇺🇬 **Ugandan food knowledge** | Built-in reference database of ~95 Ugandan dishes, street foods, fruits, drinks and shop items, with local names (Luganda/Acholi/Swahili). The AI is primed with it so portions and values match how food is actually served in Uganda. It also works **offline** as a searchable food list. |
| 🔥 **Daily calorie count** | Counts how many meals you've logged today, totals calories, and shows the **difference from your daily calorie goal** (kcal left or kcal over). |
| 🥑 **Keto verdict** | Each meal is rated ✅ *Keto friendly*, ⚠️ *Caution* or ❌ *Not keto friendly* from its **net carbs** (carbs − fibre). Before saving, KETO WATCH tells you whether the meal **will push you over today's net-carb limit** and knock you out of ketosis. |
| ⚙️ **Set-up wizard** | On first launch: name, daily calorie goal (type it, or use the built-in calculator based on sex/age/weight/height/activity), and keto net-carb limit. |
| 📅 **History** | Last 30 days: meals per day, calories against your goal, keto days, average intake. |
| 📲 **Easy sharing** | *Settings → Share the app* sends the installable APK itself through WhatsApp, Bluetooth, Nearby Share, Xender, email, and other apps. You can also share a meal (photo + calories + keto verdict) or a daily summary. |

## Lightweight by design

* Jetpack Compose UI with no image-loading, navigation, DI or database libraries. Storage uses Android's built-in SQLite and SharedPreferences.
* Uses the phone's own camera app (no camera permission or CameraX), and the system photo picker.
* Photos are shrunk to 1024 px (~100–200 KB) before upload, so scans work on slow mobile data.
* Release builds use R8 shrinking. Only the INTERNET permission is requested.
* Min Android 8.0 (API 26).

## How recognition works

`core/…/FoodRecognizer.kt` sends the downscaled photo to the Claude API using the official
[Anthropic Java SDK](https://github.com/anthropics/anthropic-java), with:

* model `claude-opus-5-5` by default, or `claude-haiku-5-5` (*Faster & cheaper*), selectable in Settings
* a system prompt specialised in Ugandan meals plus the full Ugandan reference table
* **structured JSON output**, so the response always parses into the meal components with grams, kcal, carbs, fibre, protein and fat
* server-side **refusal fallbacks** (beta `server-side-fallback-2026-07-01`, `fallbacks: "default"`) on Opus

Keto rules (`core/…/KetoRules.kt`):

* **Keto friendly**: ≤ 6 g net carbs per meal and net carbs ≤ 20 % of the meal's energy
* **Caution**: ≤ 12 g net carbs
* **Not keto friendly**: > 12 g net carbs
* **Daily impact**: compares the meal's net carbs with what you've already eaten today and your daily limit (default 25 g)

### API key

Each user enters their own Claude API key from [console.anthropic.com](https://console.anthropic.com) during set-up or
in Settings. The key is stored only in the app's private storage. Without a key, users can still log meals from the
Ugandan food list.

For a personal build you can bake in a default key by adding `ANTHROPIC_API_KEY=sk-ant-...` to `local.properties`.
**Don't share APKs built that way.** Anyone who has the APK can extract the key.

## Build

### Option A: Android Studio
1. Open the `keto-watch` folder in Android Studio (Ladybug or newer).
2. Let Gradle sync, then press **Run ▶** with a phone connected (USB debugging on) or an emulator.
3. For an installable file: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

### Option B: command line
```bash
./gradlew :core:test              # run the unit tests for the food database, keto rules and calorie maths
./gradlew :app:assembleRelease    # → app/build/outputs/apk/release/app-release.apk
```
Requires JDK 17+ and the Android SDK (`ANDROID_HOME` or `local.properties` → `sdk.dir=...`).

### Option C: GitHub (no Android Studio needed)
Push this folder to a GitHub repository. The workflow in `.github/workflows/build-apk.yml` runs the tests and builds
the APKs. Download them from the run's **Artifacts** section.

### Release signing
Without extra setup, the release APK is signed with the debug key, so you can install and share it, but you can't publish it to Play Store.
To sign with your own key, create `keystore.properties` in the project root:
```
storeFile=keto-watch.jks
storePassword=...
keyAlias=ketowatch
keyPassword=...
```

## Installing on a friend's phone
Open the APK received over WhatsApp or Bluetooth. Android asks to allow installs from that app once (*Install unknown apps*). Then tap **Install**.

## Project structure
```
core/   Pure Kotlin (unit-tested on the JVM)
  Models.kt              Nutrition, FoodItem, MealAnalysis
  UgandaFoodDatabase.kt  ~95 Ugandan foods, drinks & shop items with local names
  KetoRules.kt           keto rating + "will this break my keto day?" logic
  Goals.kt               calorie goal calculator (Mifflin–St Jeor), daily summary
  FoodRecognizer.kt      Claude vision call + JSON parsing
app/    Android (Jetpack Compose)
  data/   SQLite meal diary, settings
  ui/     Onboarding, Today, Scan result, Food list, History, Settings
  util/   photo downscaling, sharing (APK / meal / summary)
```

## Disclaimer
Nutrition values from photos and from the reference list are estimates. They are not medical advice.
