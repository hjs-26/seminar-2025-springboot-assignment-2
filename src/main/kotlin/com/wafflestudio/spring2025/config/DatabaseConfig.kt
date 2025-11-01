package com.wafflestudio.spring2025.config

import com.wafflestudio.spring2025.common.enum.SemesterReadConverter
import com.wafflestudio.spring2025.common.enum.SemesterWriteConverter
import com.wafflestudio.spring2025.course.model.ClassTimeJsonReadConverter
import com.wafflestudio.spring2025.course.model.ClassTimeJsonWriteConverter
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import javax.sql.DataSource

@Configuration
@EnableJdbcRepositories(basePackages = ["com.wafflestudio.spring2025"])
@EnableJdbcAuditing
class DatabaseConfig(
    private val env: Environment,
    private val semesterReadConverter: SemesterReadConverter,
    private val semesterWriteConverter: SemesterWriteConverter,
    private val classTimeJsonReadConverter: ClassTimeJsonReadConverter,
    private val classTimeJsonWriteConverter: ClassTimeJsonWriteConverter,
) : AbstractJdbcConfiguration() {
    @Bean
    fun dataSource(): DataSource =
        DataSourceBuilder
            .create()
            .url(env.getProperty("spring.datasource.url"))
            .username(env.getProperty("spring.datasource.username"))
            .password(env.getProperty("spring.datasource.password"))
            .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
            .build()

    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                semesterReadConverter,
                semesterWriteConverter,
                classTimeJsonReadConverter,
                classTimeJsonWriteConverter,
            ),
        )
}
