package st.orm.demo.imdb

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.thymeleaf.Thymeleaf
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.koin.ktor.ext.getKoin
import org.thymeleaf.context.IExpressionContext
import org.thymeleaf.linkbuilder.StandardLinkBuilder
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import st.orm.demo.imdb.service.ImdbDataImporter
import st.orm.demo.imdb.web.configureRouting
import st.orm.ktor.Storm
import st.orm.serialization.StormSerializers

/**
 * The application module: plugin setup, service wiring (Koin.kt), routing
 * (web/Routing.kt), and the streaming import on first startup.
 *
 * Referenced from application.conf (ktor.application.modules) and started by
 * Ktor's EngineMain.
 */
fun Application.module() {
    configureStorm()
    configureKoin()
    configureSerialization()
    configureTemplating()
    configureRouting()

    // Import the dataset on first startup (skipped once movie data is present),
    // after Flyway and the Storm plugin are ready. Blocking startup until it
    // finishes is intentional — the Ktor counterpart of Spring's ApplicationRunner.
    getKoin().get<ImdbDataImporter>().import()
}

/**
 * The plugin creates the connection pool from application.conf
 * (storm.datasource) and validates every entity against the live database
 * schema at startup, failing fast on any mismatch (the default; the production
 * counterpart of the validateSchema() test). The migration hook runs Flyway
 * first, so validation always sees the migrated schema.
 */
private fun Application.configureStorm() {
    install(Storm) {
        migration { Flyway.configure().dataSource(it).load().migrate() }
    }
}

/**
 * StormSerializers handles Ref fields; every response shape survives the
 * kotlinx.serialization round-trip unchanged.
 */
private fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json { serializersModule = StormSerializers })
    }
}

private fun Application.configureTemplating() {
    install(Thymeleaf) {
        setTemplateResolver(
            ClassLoaderTemplateResolver().apply {
                prefix = "templates/"
                suffix = ".html"
                characterEncoding = "UTF-8"
            }
        )
        // Ktor renders Thymeleaf with a non-web context; resolve context-relative
        // @{/...} links against the application root (empty context path).
        setLinkBuilder(RootContextLinkBuilder())
    }
}

/**
 * Link builder that resolves context-relative URLs (@{/...}) without a servlet
 * context. Ktor serves from the application root, so the context path is empty
 * and such links pass through unchanged. The default StandardLinkBuilder throws
 * for /-relative links unless the Thymeleaf context is a web context, which
 * Ktor's plain context is not.
 */
private class RootContextLinkBuilder : StandardLinkBuilder() {
    override fun computeContextPath(
        context: IExpressionContext?,
        base: String?,
        parameters: MutableMap<String, Any>?
    ): String = ""
}
