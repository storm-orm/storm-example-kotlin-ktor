package st.orm.demo.imdb.service

import st.orm.Page
import st.orm.demo.imdb.model.Watchlist
import st.orm.demo.imdb.repository.MovieRepository
import st.orm.demo.imdb.repository.WatchlistRepository
import st.orm.template.transaction
import java.time.Instant

class WatchlistService(
    private val movieRepository: MovieRepository,
    private val watchlistRepository: WatchlistRepository
) {

    /**
     * Adds the movie to the watchlist, or removes it when already present.
     * Returns whether the movie is on the watchlist afterwards.
     *
     * The exists/insert/remove cycle runs in an explicit Storm transaction:
     * the boundary is visible in the code and there is no AOP proxy that
     * could silently skip it. Storm manages the transaction directly on the
     * DataSource — no framework transaction manager is involved.
     */
    suspend fun toggle(movieId: String): Boolean = transaction {
        val movie = movieRepository.getById(movieId)
        if (watchlistRepository.existsById(movie)) {
            watchlistRepository.removeById(movie)
            false
        } else {
            watchlistRepository.insert(Watchlist(movie = movie, addedAt = Instant.now()))
            true
        }
    }

    /** One page of the watchlist for the watchlist page (0-based). */
    fun findPage(pageNumber: Int): Page<Watchlist> =
        watchlistRepository.findPage(pageNumber, WATCHLIST_PAGE_SIZE)

    companion object {
        private const val WATCHLIST_PAGE_SIZE = 12
    }
}
