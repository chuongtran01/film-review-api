# ProGuard rules for Film Review Backend

# Keep TMDB API wrapper model classes
# These classes are used for deserializing JSON responses from TMDB API
-keep class info.movito.themoviedbapi.model.** { *; }

# Keep TMDB API wrapper core classes
-keep class info.movito.themoviedbapi.** { *; }

# Keep annotations used by TMDB models
-keepattributes *Annotation*

# Keep source file names and line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# If you keep the line numbers, you can have stack traces like this:
#   java.lang.RuntimeException: Exception at com.filmreview.service.TmdbService.getMovieDetails(TmdbService.java:50)
-keepattributes SourceFile,LineNumberTable
