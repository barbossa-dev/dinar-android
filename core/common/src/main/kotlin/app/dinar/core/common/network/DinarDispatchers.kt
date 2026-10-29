package app.dinar.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val dinarDispatcher: DinarDispatchers)

enum class DinarDispatchers { Default, IO }
