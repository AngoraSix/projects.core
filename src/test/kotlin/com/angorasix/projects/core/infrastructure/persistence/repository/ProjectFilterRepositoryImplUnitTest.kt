package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Flux

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectFilterRepositoryImplUnitTest {

    private lateinit var filterRepoImpl: ProjectFilterRepositoryImpl

    @MockK
    private lateinit var mongoOps: ReactiveMongoOperations

    val slot = slot<Query>()

    @BeforeEach
    fun init() {
        filterRepoImpl = ProjectFilterRepositoryImpl(mongoOps)
    }

    @Test
    @Throws(Exception::class)
    fun `Given empty ProjectFilter - When findUsingFilter - Then find repo operation with empty query`() =
        runBlockingTest {
            val filter = ListProjectsFilter()
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).isEmpty()
        }

    @Test
    @Throws(Exception::class)
    fun `Given populated ProjectFilter - When findUsingFilter - Then find repo operation with populated query`() =
        runBlockingTest {
            val filter = ListProjectsFilter(listOf("1", "2"), "adminId1")
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("_id").containsKey("adminId")
        }
}
