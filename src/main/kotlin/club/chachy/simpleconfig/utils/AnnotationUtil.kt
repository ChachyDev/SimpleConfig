package club.chachy.simpleconfig.utils

import java.lang.reflect.Field
import kotlin.reflect.KClass

fun KClass<out Annotation>.isPresent(field: Field) = field.isAnnotationPresent(java)