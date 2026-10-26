# StayFinder

Platformă de descoperire și rezervare de cazări cu **feed personalizat** în stil social
(carduri, favorite, recenzii). Un singur backend **Spring Boot** servește atât aplicația
**Android** (API REST + JWT), cât și **interfața web** (Thymeleaf).

> Proiect pentru disciplinele *Calcul mobil* și *Proiectarea aplicațiilor web*.

## Stadiu

| Fază | Conținut | Stare |
|------|----------|-------|
| 1 | Backend de bază: entități, import JSON, GET cazări, Swagger | ✅ |
| 2 | Android: listă + detaliu, navigare | ✅ |
| 3 | Android: Room, onboarding | ✅ |
| 4 | Backend: JWT, preferințe, favorite, rezervări, recomandări | ✅ |
| 5 | Android ↔ backend (Retrofit), feed personalizat | ⏳ |
| 6 | Web Thymeleaf + panou admin | ⏳ |
| 7 | Finisaje + funcționalitate avansată | ⏳ |

## Funcționalități (faza 1 & faza 4)

- 60 de cazări reale/plauzibile (munte, mare, oraș) importate automat la pornire
- Tabel `SimilarRegion` pre-populat (ex. Dolomiți IT ↔ Dolomiții Lienz AT)
- Căutare cu filtre combinabile, sortare și paginare
- Autentificare stateless JWT (BCrypt, token HS256, filtre Spring Security)
- Conturi implicite pre-configurate:
  - **Admin**: `admin@stayfinder.com` / `admin123`
  - **Utilizator**: `user@stayfinder.com` / `user123`
- Motor de recomandare personalizat (`GET /api/feed`) cu transparență decizională:
  - Formula de scor ponderat:
    `score = 0.30 * typeMatch + 0.20 * budgetMatch + 0.10 * allInclusiveMatch + 0.20 * tagAffinity + 0.15 * regionDiversityBonus + 0.05 * normalizedRating - penalties`
  - Bonus diversitate regională (ex. Dolomiți Italia -> Dolomiți Austria într-o țară nouă)
  - Penalizări automate (aceeași regiune vizitată, cazare vizualizată repetat fără interacțiune)
  - Cazările deja rezervate confirmat sunt excluse automat din feed
  - Motiv textual explicativ atașat fiecărei recomandări (`reason`)
- Gestionare completă preferințe, favorite și rezervări pentru utilizatorul logat (`/api/me/**`)
- Operațiuni CRUD de administrare protejate prin rol (`/api/admin/accommodations/**`)
- Format JSON uniform pentru erori și răspunsuri paginate
- Documentație interactivă Swagger UI cu suport Bearer JWT

## API (faza 1 & faza 4)

| Metodă | Endpoint | Acces | Descriere |
|--------|----------|-------|-----------|
| POST | `/api/auth/register` | Public | Înregistrare cont nou |
| POST | `/api/auth/login` | Public | Autentificare și primire token JWT |
| GET | `/api/accommodations` | Public | Listă paginată cu filtre opționale și sortare |
| GET | `/api/accommodations/{id}` | Public | Detaliu cazare (înregistrează ViewEvent dacă ești autentificat) |
| GET | `/api/feed` | User | Feed personalizat de recomandări cu scor și motiv |
| GET / PUT | `/api/me/preferences` | User | Vizualizare / actualizare preferințe de călătorie |
| GET / POST / DELETE | `/api/me/favorites[/{id}]` | User | Listare, adăugare și ștergere cazări favorite |
| GET / POST | `/api/me/bookings` | User | Listare și creare rezervare nouă |
| PUT | `/api/me/bookings/{id}/cancel` | User | Anulare rezervare |
| POST / PUT / DELETE | `/api/admin/accommodations[/{id}]` | Admin | Adăugare, editare și ștergere cazare |

Exemplu: `GET /api/accommodations?type=MOUNTAIN&country=Austria&maxPrice=200&page=0&size=10`

## Rulare backend

Cerințe: **JDK 17+** (Maven nu trebuie instalat — proiectul include Maven Wrapper).

```powershell
cd backend
.\mvnw.cmd spring-boot:run        # Windows
# ./mvnw spring-boot:run          # Linux / macOS
```

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Consola H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:stayfinder`, user `sa`, fără parolă)
- Teste: `.\mvnw.cmd test`

### PostgreSQL (opțional)

1. Copiază `backend/src/main/resources/application-local.properties.example` în
   `application-local.properties` (ignorat de git) și completează `DB_PASSWORD`
   — sau setează variabilele de mediu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. Rulează cu profilul `postgres`:
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
   ```

## Structura proiectului

```
pawcmo/
├── backend/                         Spring Boot (Maven)
│   └── src/main/java/com/stayfinder/
│       ├── config/                  DataSeeder (import JSON), OpenApiConfig
│       ├── controller/              REST controllers (/api/...)
│       ├── dto/                     Obiecte expuse prin API (nu entitățile)
│       ├── exception/               GlobalExceptionHandler + excepții proprii
│       ├── model/                   Entități JPA + enum-uri
│       ├── repository/              Spring Data JPA + Specifications (filtre)
│       └── service/                 Logica de business
│   └── src/main/resources/
│       ├── application.properties           H2 (dev)
│       ├── application-postgres.properties  profil PostgreSQL
│       └── data/                            accommodations.json, similar-regions.json
└── android/                         Aplicația Android (Kotlin + Jetpack Compose)
    └── app/src/main/java/com/stayfinder/app/
        ├── model/                   Modele de domeniu (Accommodation, UserPreferences, enum-uri)
        ├── data/                    Repository-uri offline-first + DataStore + AppContainer
        │   └── db/                  Room (StayFinderDatabase, Entities, DAOs)
        └── ui/
            ├── components/          Carduri și stări reutilizabile (Loading/Error/Empty)
            ├── onboarding/          Onboarding în 4 pași + OnboardingViewModel
            ├── feed/                Ecranul „Pentru tine" + FeedViewModel
            ├── detail/              Ecranul de detaliu cazare + DetailViewModel
            ├── favorites/           Ecranul de favorite + FavoritesViewModel
            ├── profile/             Profil + editare preferințe
            ├── navigation/          Routes + StayFinderNavHost (Navigation Compose)
            └── theme/               Material 3 (culori turcoaz/coral, dark mode, forme)
```

## Rulare Android (faza 2)

1. Deschide folderul `android/` în **Android Studio** (sau rulează din linia de comandă).
2. Pentru linia de comandă:
   ```powershell
   cd android
   .\gradlew.bat assembleDebug testDebugUnitTest
   ```
   APK-ul generat se află în `android/app/build/outputs/apk/debug/app-debug.apk`.

## Securitate

Nicio parolă sau cheie nu este pusă în repository. Secretele se citesc din
`application-local.properties` / `local.properties` (ambele în `.gitignore`)
sau din variabile de mediu. Vezi fișierele `*.example`.
