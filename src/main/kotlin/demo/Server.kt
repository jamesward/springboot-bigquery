package demo

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.QueryJobConfiguration

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

data class Question(val url: String, val title: String, val body: String, val tags: Sequence<String>, val viewCount: Int, val favoriteCount: Int)

@SpringBootApplication
@RestController
class Server(val bigQuery: BigQuery) {

    @GetMapping
    fun index() = run {
        val query = """
                SELECT CONCAT('https://stackoverflow.com/questions/', CAST(id as STRING)) as url, title, body, tags, view_count, favorite_count
                FROM `bigquery-public-data.stackoverflow.posts_questions`
                ORDER BY favorite_count DESC
                LIMIT 20
                """

        val queryConfig = QueryJobConfiguration.newBuilder(query).build()

        bigQuery.query(queryConfig).iterateAll().iterator().asSequence().map { row ->
            Question(
                row.get("url").stringValue,
                row.get("title").stringValue,
                row.get("body").stringValue,
                row.get("tags").stringValue.splitToSequence('|'),
                row.get("view_count").longValue.toInt(),
                row.get("favorite_count").longValue.toInt(),
            )
        }
    }

}

fun main(args: Array<String>) {
    runApplication<Server>(*args)
}
