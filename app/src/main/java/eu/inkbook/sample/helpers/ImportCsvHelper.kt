package eu.inkbook.sample.helpers

import eu.inkbook.sample.data.DataEntity
import java.io.File

object ImportCsvHelper {
    fun save(file: File): ArrayList<DataEntity> {
        val dtos = ArrayList<DataEntity>()
        if(file.exists()) {
            val iter = file.readLines().listIterator()
            iter.next()
            while (iter.hasNext()) {
                val next = iter.next()

                try {
                    val split = next.split(",")
                    var cumulative = 0.0
                    if (split[11] == null)
                        cumulative = split[11].toDouble()
                    val entity = DataEntity(
                        split[0],
                        split[1].toInt(),
                        split[2].toInt(),
                        split[3].toInt(),
                        split[4].toInt(),
                        split[5].toInt(),
                        split[6],
                        split[7],
                        split[8],
                        split[9].toLong(),
                        split[10],
                        cumulative
                    )
                    dtos.add(entity)
                } catch (e: Exception) {
//                Log.w("helper", e.message + " ${next}")
                }
            }
        }
        return dtos
    }
}