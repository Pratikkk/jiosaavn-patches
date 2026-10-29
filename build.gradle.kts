allprojects {
    group = "app.morphe.patches.jiosaavn"
    version = "1.0.0"

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}