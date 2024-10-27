package com.angorasix.projects.core.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.core.domain.project.Project
import com.angorasix.projects.core.infrastructure.queryfilters.ListProjectsFilter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
    fun `Given empty ProjectFilter - When findByIdForContributor - Then find repo operation filtering out private projects`() =
        runTest {
            val filter = ListProjectsFilter()
            val queryOutput: Flux<Project> = Flux.empty()
            every { mongoOps.find(capture(slot), Project::class.java) } returns queryOutput

            val output = filterRepoImpl.findForContributorUsingFilter(filter, null)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("\$or")
            val orCriteria = capturedQuery.queryObject["\$or"] as List<Map<String, Any>>
            assertThat(orCriteria)
                .anyMatch { it.containsKey("\$and") }
            val andCriteriaElement = orCriteria.find {
                it.containsKey("\$and")
            } as Map<String, Any>
            val andCriteria = andCriteriaElement["\$and"] as List<Map<String, Any>>
            assertThat(andCriteria).anyMatch { it.containsKey("admins") }
                .anyMatch { it["private"] == false }
            assertThat(output).isNull()
        }

    @Test
    @Throws(Exception::class)
    fun `Given empty ProjectFilter - When findUsingFilter for contributor - Then find repo operation filtering out private projects`() =
        runTest {
            val simpleContributor = SimpleContributor("mockedAdminId", emptySet())
            val filter = ListProjectsFilter()
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter, simpleContributor)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("\$or")
            val orCriteria = capturedQuery.queryObject["\$or"] as List<Map<String, Any>>
            assertThat(orCriteria)
                .anyMatch { it.containsKey("\$and") }
            val andCriteriaElement = orCriteria.find {
                it.containsKey("\$and")
            } as Map<String, Any>
            val andCriteria = andCriteriaElement["\$and"] as List<Map<String, Any>>
            assertThat(andCriteria)
                .anyMatch { it.containsKey("admins") }
                .anyMatch { it["private"] == false }
        }

    @Test
    @Throws(Exception::class)
    fun `Given empty ProjectFilter - When findUsingFilter - Then find repo operation filtering out private projects`() =
        runTest {
            val filter = ListProjectsFilter()
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter, null)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("\$or")
            val orCriteria = capturedQuery.queryObject["\$or"] as List<Map<String, Any>>
            assertThat(orCriteria)
                .anyMatch { it.containsKey("\$and") }
            val andCriteriaElement = orCriteria.find {
                it.containsKey("\$and")
            } as Map<String, Any>
            val andCriteria = andCriteriaElement["\$and"] as List<Map<String, Any>>
            assertThat(andCriteria).anyMatch { it.containsKey("admins") }
                .anyMatch { it["private"] == false }
        }

    @Test
    @Throws(Exception::class)
    fun `Given empty ProjectFilter - When findUsingFilter for own user - Then find repo operation filtering out adminId projects`() =
        runTest {
            val filter = ListProjectsFilter(null, listOf("mockedAdminId"), true)
            val simpleContributor = SimpleContributor("mockedAdminId", emptySet())
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter, simpleContributor)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("\$or")
            val orCriteria = capturedQuery.queryObject["\$or"] as List<Map<String, Any>>
            assertThat(orCriteria)
                .anyMatch { it.containsKey("\$and") }
            val andCriteriaElement = orCriteria.find {
                it.containsKey("\$and")
            } as Map<String, Any>
            val andCriteria = andCriteriaElement["\$and"] as List<Map<String, Any>>
            assertThat(andCriteria).anyMatch { it.containsKey("admins") }
                .anyMatch { it["private"] == true }
        }

    @Test
    @Throws(Exception::class)
    fun `Given populated ProjectFilter - When findUsingFilter - Then find repo operation with populated query`() =
        runTest {
            val filter = ListProjectsFilter(listOf("1", "2"), listOf("adminId1"))
            val mockedFlux = mockk<Flux<Project>>()
            every { mongoOps.find(capture(slot), Project::class.java) } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter, null)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, Project::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("\$or")
            val orCriteria = capturedQuery.queryObject["\$or"] as List<Map<String, Any>>
            assertThat(orCriteria)
                .anyMatch { it.containsKey("\$and") }
            val andCriteriaElement = orCriteria.find {
                it.containsKey("\$and")
            } as Map<String, Any>
            val andCriteria = andCriteriaElement["\$and"] as List<Map<String, Any>>
            assertThat(andCriteria).anyMatch {
                it.containsKey("admins")
            }
        }
}
