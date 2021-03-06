package nz.co.jedsimson.lgp.test.modules

import nz.co.jedsimson.lgp.core.environment.dataset.Targets
import nz.co.jedsimson.lgp.core.evolution.fitness.FitnessContext
import nz.co.jedsimson.lgp.core.evolution.fitness.FitnessContexts.SingleOutputFitnessContext
import nz.co.jedsimson.lgp.core.modules.*
import nz.co.jedsimson.lgp.core.program.Outputs
import nz.co.jedsimson.lgp.core.program.ProgramGenerator
import nz.co.jedsimson.lgp.test.mocks.MockEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.lang.Exception

object ModulesFeature : Spek({

    Feature("Loading modules from the module container") {
        Scenario("Loading a module that is not in the container") {
            lateinit var moduleContainer: ModuleContainer<Double, Outputs.Single<Double>, Targets.Single<Double>>
            lateinit var moduleFactory: ModuleFactory<Double, Outputs.Single<Double>, Targets.Single<Double>>
            var exception: Exception? = null
            var fitnessContext: FitnessContext<Double, Outputs.Single<Double>, Targets.Single<Double>>? = null

            Given("A module container with no modules") {
                moduleContainer = ModuleContainer(modules = mutableMapOf())
                moduleFactory = CachingModuleFactory(moduleContainer)
            }

            When("A module is loaded") {
                try {
                    fitnessContext = moduleFactory.instance(CoreModuleType.FitnessContext)
                }
                catch (ex: Exception) {
                    exception = ex
                }
            }

            Then("No module should be loaded") {
                assert(fitnessContext == null) { "Module was not null" }
                assert(exception != null) { "Exception was null" }
                assert(exception is MissingModuleException) { "Exception is not the correct type" }
            }
        }

        Scenario("Loading a module that is in the container") {
            lateinit var moduleContainer: ModuleContainer<Double, Outputs.Single<Double>, Targets.Single<Double>>
            lateinit var moduleFactory: ModuleFactory<Double, Outputs.Single<Double>, Targets.Single<Double>>
            var fitnessContext: FitnessContext<Double, Outputs.Single<Double>, Targets.Single<Double>>? = null
            var fitnessContextFromCache: FitnessContext<Double, Outputs.Single<Double>, Targets.Single<Double>>? = null

            Given("A module container with a single module") {
                // For some reason the compiler can't figure out this type inference, so an explicit type is needed...
                moduleContainer = ModuleContainer<Double, Outputs.Single<Double>, Targets.Single<Double>>(
                    modules = mutableMapOf(
                        CoreModuleType.FitnessContext to { environment -> SingleOutputFitnessContext(environment) }
                    )
                )
                moduleContainer.environment = MockEnvironment()

                moduleFactory = CachingModuleFactory(moduleContainer)
            }

            When("The module is loaded") {
                fitnessContext = moduleFactory.instance(CoreModuleType.FitnessContext)
            }

            Then("The module should be loaded") {
                assert(fitnessContext != null) { "Module was null" }
            }

            When("The module is loaded again") {
                fitnessContextFromCache = moduleFactory.instance((CoreModuleType.FitnessContext))
            }

            Then("It should be loaded from the cache") {
                assert(fitnessContextFromCache != null) { "Module was null" }
                assert(fitnessContext == fitnessContextFromCache) { "Module was not returned from cache" }
            }
        }

        Scenario("Loading module with an invalid cast type") {
            lateinit var moduleContainer: ModuleContainer<Double, Outputs.Single<Double>, Targets.Single<Double>>
            lateinit var moduleFactory: ModuleFactory<Double, Outputs.Single<Double>, Targets.Single<Double>>
            var exception: Exception? = null
            var programGenerator: ProgramGenerator<Int, Outputs.Single<Int>, Targets.Single<Int>>? = null

            Given("A module container with a single module") {
                // For some reason the compiler can't figure out this type inference, so an explicit type is needed...
                moduleContainer = ModuleContainer<Double, Outputs.Single<Double>, Targets.Single<Double>>(
                    modules = mutableMapOf(
                        CoreModuleType.FitnessContext to { environment -> SingleOutputFitnessContext(environment) }
                    )
                )
                moduleContainer.environment = MockEnvironment()

                moduleFactory = CachingModuleFactory(moduleContainer)
            }

            When("The module is loaded") {
                try {
                    programGenerator = moduleFactory.instance(CoreModuleType.FitnessContext)
                }
                catch (ex: Exception) {
                    exception = ex
                }
            }

            Then("The module should not be loaded") {
                assert(programGenerator == null) { "Module was not null ($programGenerator)" }
                assert(exception != null) { "Exception was null" }
                assert(exception is ModuleCastException) { "Exception was not correct type (was ${exception!!::class.java.simpleName})" }
            }
        }
    }
})