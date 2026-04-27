package com.velopass.app.data

import com.velopass.app.models.BikeEntity
import com.velopass.app.models.Bike
import com.velopass.app.models.RegistrationNumbers
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class BikeRepository @Inject constructor(
    private val bikeDao: BikeDao,
    private val httpClient: HttpClient
) {
    suspend fun registerBike(
        bike: Bike,
        userId: String,
        nationalityCode: String,
        legalResidenceCode: String
    ): Bike {
        // Generate registration numbers via AG-03
        val regNumbers = generateRegistrationNumbers(
            bikeId = bike.bikeId,
            frameType = bike.frameType,
            frameMaterial = bike.frameMaterial,
            userId = userId,
            nationalityCode = nationalityCode,
            legalResidenceCode = legalResidenceCode
        )

        // Create complete bike with registration numbers
        val completeBike = bike.copy(
            shortRegNumber = regNumbers.short,
            fullRegNumber = regNumbers.full,
            ownerId = userId,
            registeredAt = java.time.Instant.now().toString(),
            updatedAt = java.time.Instant.now().toString()
        )

        // Save to local database
        val bikeEntity = completeBike.toBikeEntity()
        bikeDao.insertBike(bikeEntity)

        // Generate PDF certificate
        generateRegistrationCertificate(bike.bikeId)

        return completeBike
    }

    private suspend fun generateRegistrationNumbers(
        bikeId: String,
        frameType: String,
        frameMaterial: String?,
        userId: String,
        nationalityCode: String,
        legalResidenceCode: String
    ): RegistrationNumbers {
        return try {
            val requestBody = buildJsonObject {
                put("userId", userId)
                put("bikeId", bikeId)
                put("frameType", frameType)
                if (frameMaterial != null) {
                    put("frameMaterial", frameMaterial)
                }
                put("nationalityCode", nationalityCode)
                put("legalResidenceCode", legalResidenceCode)
            }

            val response: HttpResponse = httpClient.post(
                "https://cloud.appwrite.io/v1/functions/generate-registration/executions"
            ) {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            val responseText = response.content.toString()
            val json = Json.parseToJsonElement(responseText).jsonObject
            RegistrationNumbers(
                short = json["short"]?.jsonPrimitive?.content ?: "",
                full = json["full"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            throw Exception("Failed to generate registration numbers: ${e.message}")
        }
    }

    private suspend fun generateRegistrationCertificate(bikeId: String): String {
        return try {
            val requestBody = buildJsonObject {
                put("bikeId", bikeId)
                put("targetLanguage", "en")
            }

            val response: HttpResponse = httpClient.post(
                "https://cloud.appwrite.io/v1/functions/generate-pdf/executions"
            ) {
                contentType(ContentType.Application.Json)
                setBody(requestBody.toString())
            }

            val responseText = response.content.toString()
            val json = Json.parseToJsonElement(responseText).jsonObject
            json["pdfUrl"]?.jsonPrimitive?.content ?: ""
        } catch (e: Exception) {
            throw Exception("Failed to generate PDF: ${e.message}")
        }
    }

    suspend fun getBike(bikeId: String): Bike? {
        return bikeDao.getBike(bikeId)?.toBike()
    }

    fun getBikesByOwner(ownerId: String): Flow<List<Bike>> {
        return bikeDao.getBikesByOwner(ownerId).map { entities ->
            entities.map { it.toBike() }
        }
    }

    suspend fun updateBike(bike: Bike) {
        val entity = bike.toBikeEntity()
        bikeDao.updateBike(entity)
    }

    suspend fun deleteBike(bikeId: String) {
        bikeDao.deleteBike(bikeId)
    }

    private fun Bike.toBikeEntity(): BikeEntity {
        return BikeEntity(
            bikeId = bikeId,
            ownerId = ownerId,
            shortRegNumber = shortRegNumber,
            fullRegNumber = fullRegNumber,
            make = make,
            model = model,
            year = year,
            frameColor = frameColor,
            frameType = frameType,
            frameMaterial = frameMaterial,
            serialNumber = serialNumber,
            description = description,
            photoUrls = Json.encodeToString(photoUrls),
            purchaseDate = purchaseDate,
            purchasePrice = purchasePrice,
            purchaseCurrency = purchaseCurrency,
            pdfFormUrl = pdfFormUrl,
            bikeIndexId = bikeIndexId,
            registeredAt = registeredAt,
            updatedAt = updatedAt
        )
    }

    private fun BikeEntity.toBike(): Bike {
        return Bike(
            bikeId = bikeId,
            ownerId = ownerId,
            shortRegNumber = shortRegNumber,
            fullRegNumber = fullRegNumber,
            make = make,
            model = model,
            year = year,
            frameColor = frameColor,
            frameType = frameType,
            frameMaterial = frameMaterial,
            serialNumber = serialNumber,
            description = description,
            photoUrls = try {
                Json.decodeFromString(photoUrls)
            } catch (e: Exception) {
                emptyList()
            },
            purchaseDate = purchaseDate,
            purchasePrice = purchasePrice,
            purchaseCurrency = purchaseCurrency,
            pdfFormUrl = pdfFormUrl,
            bikeIndexId = bikeIndexId,
            registeredAt = registeredAt,
            updatedAt = updatedAt
        )
    }
}
