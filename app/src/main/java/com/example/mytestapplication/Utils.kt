package com.example.mytestapplication

import android.util.Log
import kotlin.reflect.full.createType

/**
 * This function is able to recover any data class that was transformed to String, with the following
 * conditions.
 * 1) The data class need to have all of its properties defined inside the primary constructor.
 * 2) All the properties must be of type Boolean or Int.
 *
 * Example of a data class and recovery:
 * data class MyClass(val isDrawn: Boolean, var number: Int, var isFull: Boolean)
 * val myData = MyClass(true, 123, false)
 * val stringOfMyData = myData.toString()
 * val recoveredData = dataClassStringToDataClass<MyClass>(stringOfMyData)
 * println("Was the data recovered correctly? ${myData == recoveredData}")
 *
 * Note: In case you want to expand this function to support more property types,
 * you need to extend the when branches.
 */
inline fun <reified T> restoreDataClassFromDataClassAsString(dataClassAsString: String): T {

    val map = dataClassAsStringToMap(dataClassAsString)

    val parameters = T::class.constructors.first().parameters
    val paramNames = parameters.map { it.name }
    val paramTypes = parameters.map { it.type }

    Log.d("restoreDataClassFrom", "The content of parameters is: $parameters")
    Log.d("restoreDataClassFrom", "The content of map is: $map")
    Log.d("restoreDataClassFrom", "The content of paramNames is: $paramNames")
    Log.d("restoreDataClassFrom", "The content of paramTypes is: $paramTypes")

    val arguments = mutableListOf<Any>()

    map.onEachIndexed { index, _ ->

        val argumentValue = when (paramTypes[index]) {
            Boolean::class.createType() -> map[paramNames[index]].toBoolean()
            Int::class.createType() -> map[paramNames[index]]?.toInt()
            else -> throw NoWhenBranchMatchedException("index = $index. Unrecognized type in paramTypes. The type passed is ${paramTypes[index]}.")
        }

        arguments.add(argumentValue ?: throw NullPointerException("index = $index. Value is null, and it shouldn't."))
    }

    return T::class.constructors.first().call(*arguments.toTypedArray())
}


/**
 * It transforms a string composed of a data class that was converted toString() into a map of
 * key-value pairs, where key=property_name and value=property_value.
 */
fun dataClassAsStringToMap(dataClassAsString: String): Map<String, String> {

    val listOfProperties = dataClassAsString.dropWhile { it != '(' }.removeSurrounding("(", ")").split(",")

    val listOfPropertyNameValuePairs =
        listOfProperties.map { it.split("=") }.map { Pair(it.first().trim(), it.last().trim()) }

    return listOfPropertyNameValuePairs.toMap()
}