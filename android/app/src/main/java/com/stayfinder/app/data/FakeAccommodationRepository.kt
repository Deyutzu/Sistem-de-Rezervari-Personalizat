package com.stayfinder.app.data

import com.stayfinder.app.model.Accommodation
import com.stayfinder.app.model.AccommodationType
import com.stayfinder.app.model.TravelType
import kotlinx.coroutines.delay

/**
 * Implementare cu date MOCK (faza 2), ca aplicatia sa functioneze fara backend.
 * Datele sunt un subset din backend/src/main/resources/data/accommodations.json.
 * delay() simuleaza latenta retelei, ca sa vedem starea de "loading" in UI.
 */
class FakeAccommodationRepository : AccommodationRepository {

    override suspend fun getAccommodations(): List<Accommodation> {
        delay(800)
        return items
    }

    override suspend fun getAccommodation(id: Long): Accommodation? {
        delay(300)
        return items.find { it.id == id }
    }

    private fun img(id: String) =
        "https://images.unsplash.com/photo-$id?auto=format&fit=crop&w=1200&q=80"

    private val items = listOf(
        Accommodation(
            id = 1, title = "Hotel Cristallo Alpin",
            description = "Hotel elegant la poalele Tofanei, cu pârtii la câteva minute și centru spa cu vedere spre Dolomiți.",
            country = "Italia", region = "Dolomiți", city = "Cortina d'Ampezzo",
            type = AccommodationType.MOUNTAIN, pricePerNight = 280.0, allInclusive = false, rating = 4.8,
            imageUrl = img("1486911278844-a81c5267e227"), latitude = 46.5405, longitude = 12.1357,
            tags = listOf("schi", "spa", "vedere panoramică", "romantic"), suitableFor = TravelType.COUPLE,
            reason = "Se potrivește cu preferința ta pentru munte"
        ),
        Accommodation(
            id = 7, title = "Dolomitenhof Lienz",
            description = "Hotel tradițional tirolez cu vedere spre Dolomiții Lienz, punct de plecare pentru trasee alpine.",
            country = "Austria", region = "Dolomiții Lienz", city = "Lienz",
            type = AccommodationType.MOUNTAIN, pricePerNight = 140.0, allInclusive = false, rating = 4.6,
            imageUrl = img("1519681393784-d120267933ba"), latitude = 46.8297, longitude = 12.7690,
            tags = listOf("drumeții", "schi", "natură", "liniște"), suitableFor = TravelType.COUPLE,
            reason = "Similar cu Dolomiții din Italia, dar într-o țară nouă"
        ),
        Accommodation(
            id = 13, title = "Zermatt Matterhorn View",
            description = "Hotel boutique într-o stațiune fără mașini, cu camere orientate spre Matterhorn.",
            country = "Elveția", region = "Valais", city = "Zermatt",
            type = AccommodationType.MOUNTAIN, pricePerNight = 380.0, allInclusive = false, rating = 4.9,
            imageUrl = img("1491555103944-7c647fd857e6"), latitude = 46.0207, longitude = 7.7491,
            tags = listOf("schi", "vedere panoramică", "romantic", "liniște"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 20, title = "Poiana Brașov Ski & Spa",
            description = "Resort all-inclusive cu acces la pârtii, piscină interioară și program pentru copii.",
            country = "România", region = "Carpații Meridionali", city = "Poiana Brașov",
            type = AccommodationType.MOUNTAIN, pricePerNight = 120.0, allInclusive = true, rating = 4.6,
            imageUrl = img("1565967511849-76a60a516170"), latitude = 45.5950, longitude = 25.5530,
            tags = listOf("schi", "spa", "piscină", "familie"), suitableFor = TravelType.FAMILY,
            reason = "În bugetul tău și all-inclusive"
        ),
        Accommodation(
            id = 27, title = "Santorini Caldera Villas",
            description = "Vile albe cu piscină privată pe marginea calderei, cu cel mai faimos apus din Egee.",
            country = "Grecia", region = "Ciclade", city = "Oia",
            type = AccommodationType.SEA, pricePerNight = 390.0, allInclusive = false, rating = 4.9,
            imageUrl = img("1570077188670-e3a8d69ac5ff"), latitude = 36.4618, longitude = 25.3753,
            tags = listOf("romantic", "vedere panoramică", "apus"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 30, title = "Dubrovnik Old Town Residence",
            description = "Reședință în interiorul zidurilor medievale, la câțiva pași de Stradun și de plaja Banje.",
            country = "Croația", region = "Dalmația", city = "Dubrovnik",
            type = AccommodationType.SEA, pricePerNight = 250.0, allInclusive = false, rating = 4.7,
            imageUrl = img("1555881400-74d7acaacd8b"), latitude = 42.6410, longitude = 18.1100,
            tags = listOf("istorie", "plajă", "cultură"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 34, title = "Belek Grand Resort",
            description = "Resort ultra all-inclusive cu plajă cu nisip, terenuri de golf și club pentru copii.",
            country = "Turcia", region = "Riviera Turcească", city = "Belek",
            type = AccommodationType.SEA, pricePerNight = 230.0, allInclusive = true, rating = 4.7,
            imageUrl = img("1520250497591-112f2f40a3f4"), latitude = 36.8625, longitude = 31.0556,
            tags = listOf("plajă", "piscină", "golf", "familie"), suitableFor = TravelType.FAMILY
        ),
        Accommodation(
            id = 41, title = "Vama Veche Seaside Guesthouse",
            description = "Pensiune boemă la doi pași de plajă, cu atmosferă relaxată și concerte seara.",
            country = "România", region = "Litoral Românesc", city = "Vama Veche",
            type = AccommodationType.SEA, pricePerNight = 45.0, allInclusive = false, rating = 4.3,
            imageUrl = img("1559827260-dc66d52bef19"), latitude = 43.7500, longitude = 28.5750,
            tags = listOf("plajă", "boem", "buget"), suitableFor = TravelType.FRIENDS
        ),
        Accommodation(
            id = 45, title = "Trastevere Boutique Hotel",
            description = "Hotel intim pe străduțele pavate din Trastevere, înconjurat de trattorii autentice.",
            country = "Italia", region = "Roma", city = "Roma",
            type = AccommodationType.CITY, pricePerNight = 180.0, allInclusive = false, rating = 4.7,
            imageUrl = img("1552832230-c0197dd311b5"), latitude = 41.8890, longitude = 12.4700,
            tags = listOf("istorie", "gastronomie", "romantic", "cultură"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 49, title = "Old Town Square Hotel",
            description = "Hotel într-o clădire barocă, cu vedere spre Orologiul Astronomic din Praga.",
            country = "Cehia", region = "Praga", city = "Praga",
            type = AccommodationType.CITY, pricePerNight = 150.0, allInclusive = false, rating = 4.6,
            imageUrl = img("1541849546-216549ae216d"), latitude = 50.0870, longitude = 14.4210,
            tags = listOf("istorie", "centru", "romantic"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 51, title = "Danube Thermal Hotel",
            description = "Hotel cu băi termale proprii și vedere spre Parlament și Dunăre.",
            country = "Ungaria", region = "Budapesta", city = "Budapesta",
            type = AccommodationType.CITY, pricePerNight = 170.0, allInclusive = false, rating = 4.7,
            imageUrl = img("1551882547-ff40c63fe5fa"), latitude = 47.4980, longitude = 19.0400,
            tags = listOf("spa", "băi termale", "romantic", "vedere panoramică"), suitableFor = TravelType.COUPLE
        ),
        Accommodation(
            id = 53, title = "Montmartre Romantic Suites",
            description = "Suite boeme pe colina Montmartre, cu vedere spre acoperișurile Parisului.",
            country = "Franța", region = "Paris", city = "Paris",
            type = AccommodationType.CITY, pricePerNight = 310.0, allInclusive = false, rating = 4.7,
            imageUrl = img("1502602898657-3e91760cbb34"), latitude = 48.8867, longitude = 2.3431,
            tags = listOf("romantic", "cultură", "gastronomie"), suitableFor = TravelType.COUPLE
        )
    )
}
