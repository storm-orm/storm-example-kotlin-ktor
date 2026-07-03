package st.orm.demo.imdb.web

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing
import org.koin.ktor.ext.getKoin

/**
 * Mounts the static resources, the server-rendered pages, and the REST routes.
 * Services come from Koin; the route functions themselves stay DI-free and
 * receive their services as parameters.
 */
fun Application.configureRouting() {
    val koin = getKoin()
    routing {
        // Static resources at the same URLs the templates reference.
        staticResources("/css", "static/css")
        staticResources("/js", "static/js")
        staticResources("/img", "static/img")

        homeRoutes(koin.get())
        topMoviesRoutes(koin.get())
        watchlistRoutes(koin.get())
        statisticsRoutes(koin.get())
        searchRoutes(koin.get())
        browseRoutes(koin.get())
        movieRoutes(koin.get(), koin.get())
        personRoutes(koin.get())
        posterRoutes(koin.get())
        galleryRoutes(koin.get())
        summaryRoutes(koin.get())
    }
}
