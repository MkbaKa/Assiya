package github.mkbaka.assiya.common.engine.exception

import java.util.concurrent.RejectedExecutionException

class AttributeEngineOverloadedException : RejectedExecutionException(
    "The attribute engine queue is full"
)