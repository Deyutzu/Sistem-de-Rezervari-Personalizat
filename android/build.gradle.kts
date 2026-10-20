// Fisierul de build al proiectului radacina: doar declara plugin-urile (apply false),
// fiecare modul (aici doar :app) le aplica efectiv.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
