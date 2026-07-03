package st.orm.demo.imdb

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import st.orm.demo.imdb.service.BrowseService
import st.orm.demo.imdb.service.HomeService
import st.orm.demo.imdb.service.ImdbDataImporter
import st.orm.demo.imdb.service.MovieService
import st.orm.demo.imdb.service.PersonGalleryService
import st.orm.demo.imdb.service.PersonService
import st.orm.demo.imdb.service.SearchService
import st.orm.demo.imdb.service.StatisticsService
import st.orm.demo.imdb.service.TopMoviesService
import st.orm.demo.imdb.service.WatchlistService
import st.orm.demo.imdb.service.WikipediaSummaryService
import st.orm.demo.imdb.service.imdbImportProperties
import st.orm.demo.imdb.web.PosterController
import st.orm.ktor.koin.stormModule
import tools.jackson.module.kotlin.jacksonObjectMapper

/**
 * Koin wiring. Storm's `stormModule()` exposes the ORMTemplate and every
 * repository the plugin auto-registered from the compile-time index, each
 * under its own interface type. Services declare repositories as plain
 * constructor parameters and are wired with Koin's constructor DSL.
 */
fun Application.configureKoin() {
    install(Koin) {
        modules(
            stormModule(),
            module {
                single { jacksonObjectMapper() }
                single { environment.config.imdbImportProperties() }
                singleOf(::HomeService)
                singleOf(::TopMoviesService)
                singleOf(::WatchlistService)
                singleOf(::StatisticsService)
                singleOf(::SearchService)
                singleOf(::BrowseService)
                singleOf(::MovieService)
                singleOf(::PersonService)
                singleOf(::WikipediaSummaryService)
                singleOf(::PersonGalleryService)
                singleOf(::PosterController)
                singleOf(::ImdbDataImporter)
            },
        )
    }
}
