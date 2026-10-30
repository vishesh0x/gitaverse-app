# GitaVerse release rules. R8 is on for release builds (see build.gradle.kts).
#
# Deliberately short. NOT needed, so not here:
#  - data.source.** : the JSON models are read through kotlinx.serialization's generated serialisers, whose own
#    consumer rules keep what they need (the DataStore models were never kept and work the same way).

# WorkManager keeps its job queue in a Room database. Room creates the generated implementation
# (androidx.work.impl.WorkDatabase_Impl) by reflection through its no-argument constructor, and R8 removes that
# constructor unless it is kept. Without this rule the release app crashes at start-up with
# "NoSuchMethodException: androidx.work.impl.WorkDatabase_Impl.<init> []".
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# WorkManager creates the reminder worker by class name from its saved job; keep its constructor explicitly.
-keep class in.visheshraghuvanshi.gitaverse.core.reminder.ReminderWorker { <init>(...); }

# The same for the midnight widget refresh.
-keep class in.visheshraghuvanshi.gitaverse.widget.MidnightWidgetWorker { <init>(...); }

# Navigation 3 saves each route by class name and restores it by reflection after process death, so the route
# classes must keep their names. Only the route classes, not the whole navigation package.
-keep class * implements androidx.navigation3.runtime.NavKey { *; }
